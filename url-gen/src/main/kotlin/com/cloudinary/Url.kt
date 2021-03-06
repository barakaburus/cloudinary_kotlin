package com.cloudinary


import com.cloudinary.config.Configuration
import com.cloudinary.transformation.*
import com.cloudinary.util.*
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private const val OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net"
private const val AKAMAI_SHARED_CDN = "res.cloudinary.com"
private const val SHARED_CDN = AKAMAI_SHARED_CDN
internal const val DEFAULT_RESOURCE_TYPE = "image"

@TransformationDsl
data class Url private constructor(
    private val config: Configuration,
    private val cloudName: String = config.cloudName,
    private val publicId: String? = null,
    private val deliveryType: String? = null,
    private val resourceType: String = DEFAULT_RESOURCE_TYPE,
    private val format: Format? = null,
    private val version: String? = null,
    private val transformation: Transformation? = null,
    private val signUrl: Boolean = false,
    private val authToken: AuthToken? = config.authToken,
    private val source: String? = null,
    private val urlSuffix: String? = null,
    private val useRootPath: Boolean = config.useRootPath,
    private val forceVersion: Boolean = true,
    private val secureDistribution: String? = config.secureDistribution,
    private val privateCdn: Boolean = config.privateCdn,
    private val shorten: Boolean = config.shorten,
    private val secure: Boolean = config.secure,
    private val cname: String? = config.cname
) {

    fun generate(source: String? = null): String? {
        require(cloudName.isNotBlank()) { "Must supply cloud_name in configuration" }
        var mutableSource = source ?: publicId ?: this.source ?: return null
        var mutableTransformation = transformation ?: Transformation()
        var mutableFormat = format

        val httpSource = mutableSource.cldIsHttpUrl()

        if (httpSource && (deliveryType.isNullOrBlank() || deliveryType == "asset")) {
            return mutableSource
        }

        if (deliveryType == "fetch" && mutableFormat != null) {
            mutableTransformation = mutableTransformation.fetchFormat(mutableFormat)
            mutableFormat = null
        }

        val transformationStr = mutableTransformation.toString()
        var signature = ""

        val finalizedSource =
            finalizeSource(mutableSource, mutableFormat, urlSuffix)
        mutableSource = finalizedSource.source
        val sourceToSign = finalizedSource.sourceToSign

        var mutableVersion = version
        if (forceVersion && sourceToSign.contains("/") && !sourceToSign.cldHasVersionString() &&
            !httpSource && mutableVersion.isNullOrBlank()
        ) {
            mutableVersion = "1"
        }

        mutableVersion = if (mutableVersion == null) "" else "v$mutableVersion"

        if (signUrl && (authToken == null || authToken == NULL_AUTH_TOKEN)) {
            val md: MessageDigest?
            md = try {
                MessageDigest.getInstance("SHA-1")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Unexpected exception", e)
            }

            val toSign = listOf(transformationStr, sourceToSign)
                .joinToString("/")
                .cldRemoveStartingChars('/')
                .cldMergeSlashedInUrl()
            val digest = md.digest((toSign + config.apiSecret!!).toByteArray(Charset.forName("UTF-8")))
            signature = Base64Coder.encodeURLSafeString(digest)
            signature = "s--" + signature.substring(0, 8) + "--"
        }

        val finalizedResourceType = finalizeResourceType(
            resourceType,
            deliveryType,
            urlSuffix,
            useRootPath,
            shorten
        )

        val prefix = unsignedDownloadUrlPrefix2(
            cloudName,
            privateCdn,
            cname,
            secure,
            secureDistribution
        )

        val url =
            listOfNotNull(
                prefix,
                finalizedResourceType,
                signature,
                transformationStr,
                mutableVersion,
                mutableSource
            ).joinToString("/").cldMergeSlashedInUrl()

        return if (signUrl && authToken != null && authToken != NULL_AUTH_TOKEN) {
            val token = authToken.generate(URL(url).path)
            "$url?$token"
        } else {
            url
        }
    }

    @TransformationDsl
    class Builder(private val config: Configuration) : ITransformable<Builder> {
        private var cloudName: String = config.cloudName
        private var publicId: String? = null
        private var deliveryType: String? = null
        private var resourceType: String = DEFAULT_RESOURCE_TYPE
        private var format: Format? = null
        private var version: String? = null
        private var transformation: Transformation? = null
        private var signUrl: Boolean = false
        private var authToken: AuthToken? = config.authToken
        private var source: String? = null
        private var urlSuffix: String? = null
        private var useRootPath: Boolean = config.useRootPath
        private var forceVersion: Boolean = true
        private var secureDistribution: String? = config.secureDistribution
        private var privateCdn: Boolean = config.privateCdn
        private var shorten: Boolean = config.shorten
        private var secure: Boolean = config.secure
        private var cname: String? = config.cname

        fun cloudName(cloudName: String) = apply { this.cloudName = cloudName }
        fun publicId(publicId: String) = apply { this.publicId = publicId }
        fun deliveryType(type: String?) = apply { this.deliveryType = type }
        fun resourceType(resourceType: String) = apply { this.resourceType = resourceType }
        fun format(format: Format?) = apply { this.format = format }
        fun version(version: String?) = apply { this.version = version }
        fun transformation(transformation: Transformation) = apply { this.transformation = transformation }
        fun signUrl(signUrl: Boolean) = apply { this.signUrl = signUrl }
        fun authToken(authToken: AuthToken) = apply { this.authToken = authToken }
        fun source(source: String?) = apply { this.source = source }
        fun urlSuffix(urlSuffix: String) = apply { this.urlSuffix = urlSuffix }
        fun useRootPath(useRootPath: Boolean) = apply { this.useRootPath = useRootPath }
        fun forceVersion(forceVersion: Boolean) = apply { this.forceVersion = forceVersion }
        fun secureDistribution(secureDistribution: String?) = apply { this.secureDistribution = secureDistribution }
        fun privateCdn(privateCdn: Boolean) = apply { this.privateCdn = privateCdn }
        fun shorten(shorten: Boolean) = apply { this.shorten = shorten }
        fun secure(secure: Boolean) = apply { this.secure = secure }
        fun cname(cname: String?) = apply { this.cname = cname }

        override fun add(action: Action) = apply { transformation = (transformation ?: Transformation()).add(action) }

        fun build() = Url(
            config,
            cloudName,
            publicId,
            deliveryType,
            resourceType,
            format,
            version,
            transformation,
            signUrl,
            authToken,
            source,
            urlSuffix,
            useRootPath,
            forceVersion,
            secureDistribution,
            privateCdn,
            shorten,
            secure,
            cname
        )
    }
}

private fun finalizeSource(
    source: String,
    format: Format?,
    urlSuffix: String?
): FinalizedSource {
    var mutableSource = source.cldMergeSlashedInUrl()
    var sourceToSign: String
    if (mutableSource.cldIsHttpUrl()) {
        mutableSource = mutableSource.cldSmartUrlEncode()
        sourceToSign = mutableSource
    } else {
        mutableSource = try {
            URLDecoder.decode(mutableSource.replace("+", "%2B"), "UTF-8").cldSmartUrlEncode()
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
        sourceToSign = mutableSource
        if (!urlSuffix.isNullOrBlank()) {
            require(!(urlSuffix.contains(".") || urlSuffix.contains("/"))) { "url_suffix should not include . or /" }
            mutableSource = "$mutableSource/$urlSuffix"
        }
        if (format != null) {
            mutableSource = "$mutableSource.$format"
            sourceToSign = "$sourceToSign.$format"
        }
    }

    return FinalizedSource(mutableSource, sourceToSign)
}

fun finalizeResourceType(
    resourceType: String?,
    type: String?,
    urlSuffix: String?,
    useRootPath: Boolean,
    shorten: Boolean
): String? {
    var mutableResourceType: String? = resourceType ?: "image"
    var mutableType: String? = type ?: "upload"

    if (!urlSuffix.isNullOrBlank()) {
        if (mutableResourceType == "image" && mutableType == "upload") {
            mutableResourceType = "images"
            mutableType = null
        } else if (mutableResourceType == "image" && mutableType == "private") {
            mutableResourceType = "private_images"
            mutableType = null
        } else if (mutableResourceType == "image" && mutableType == "authenticated") {
            mutableResourceType = "authenticated_images"
            mutableType = null
        } else if (mutableResourceType == "raw" && mutableType == "upload") {
            mutableResourceType = "files"
            mutableType = null
        } else if (mutableResourceType == "video" && mutableType == "upload") {
            mutableResourceType = "videos"
            mutableType = null
        } else {
            throw IllegalArgumentException("URL Suffix only supported for image/upload, image/private, raw/upload, image/authenticated  and video/upload")
        }
    }
    if (useRootPath) {
        if (mutableResourceType == "image" && mutableType == "upload" || mutableResourceType == "images" && mutableType.isNullOrBlank()) {
            mutableResourceType = null
            mutableType = null
        } else {
            throw IllegalArgumentException("Root path only supported for image/upload")
        }
    }
    if (shorten && mutableResourceType == "image" && mutableType == "upload") {
        mutableResourceType = "iu"
        mutableType = null
    }
    var result = mutableResourceType
    if (mutableType != null) {
        result += "/$mutableType"
    }

    return result
}

fun unsignedDownloadUrlPrefix2(
    cloudName: String?,
    privateCdn: Boolean,
    cname: String?,
    secure: Boolean,
    secureDistribution: String?
): String? {
    var mutableCloudName = cloudName
    var mutableSecureDistribution = secureDistribution
    mutableCloudName?.let {
        if (it.startsWith("/")) return "/res$mutableCloudName"
    }

    var sharedDomain: Boolean = !privateCdn
    var prefix: String
    if (secure) {
        if (mutableSecureDistribution.isNullOrBlank() || mutableSecureDistribution == OLD_AKAMAI_SHARED_CDN) {
            mutableSecureDistribution =
                if (privateCdn) mutableCloudName.toString() + "-res.cloudinary.com" else SHARED_CDN
        }
        if (!sharedDomain) {
            sharedDomain = mutableSecureDistribution == SHARED_CDN
        }

        prefix = "https://$mutableSecureDistribution"
    } else if (!cname.isNullOrBlank()) {
        prefix = "http://$cname"
    } else {
        val protocol = "http://"
        mutableCloudName = if (privateCdn) "$mutableCloudName-" else ""
        val res = "res"
        val domain = ".cloudinary.com"
        prefix = protocol + mutableCloudName + res + domain
    }
    if (sharedDomain) {
        // use original cloud name here:
        prefix += "/$cloudName"
    }

    return prefix
}

private class FinalizedSource(val source: String, val sourceToSign: String)

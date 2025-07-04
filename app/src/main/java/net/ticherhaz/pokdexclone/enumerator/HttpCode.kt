package net.ticherhaz.pokdexclone.enumerator

import net.ticherhaz.pokdexclone.R

enum class HttpCode(val code: Int, val messageInt: Int) {
    UNAUTHORIZED(401, R.string.unauthorized),
    NOT_FOUND(404, R.string.not_found),
    SERVER_ERROR(500, R.string.server_error),
    UNKNOWN_ERROR(501, R.string.unknown_error),
    RETRY_TO_CONNECT(1006, R.string.retry_to_connect)
}
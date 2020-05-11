!/////////////////////////////////////////////////////////////////////////
!// file: response_codes.inc                                            //
!// This file is distributed as part of OpenSTA - http://opensta.org/   //
!// It is included in all generated scripts and contains HTTP response  //
!// codes and other constant definitions.                               //
!/////////////////////////////////////////////////////////////////////////
CONSTANT    CONTINUE = 100                  ! Continue
CONSTANT    SWITCH_PROTOCOLS = 101          ! Switching Protocols
CONSTANT    OK = 200                        ! OK
CONSTANT    CREATED = 201                   ! Created
CONSTANT    ACCEPTED = 202                  ! Accepted
CONSTANT    NON_AUTH_INFO = 203             ! Non-Authoritative Information
CONSTANT    NO_CONTENT = 204                ! No Content
CONSTANT    RESET_CONTENT = 205             ! Reset Content
CONSTANT    PARTIAL_CONTENT = 206           ! Partial Content
CONSTANT    MULTIPLE_CHOICES = 300          ! Multiple Choices
CONSTANT    MOVED = 301                     ! Moved Permanently
CONSTANT    FOUND = 302                     ! Found
CONSTANT    SEE_OTHER = 303                 ! See Other
CONSTANT    NOT_MODIFIED = 304              ! Not Modified
CONSTANT    USE_PROXY = 305                 ! Use Proxy
CONSTANT    TEMPORARY = 307                 ! Temporary Redirect
CONSTANT    BAD_REQ = 400                   ! Bad Request
CONSTANT    UNAUTHORIZED = 401              ! Unauthorized
CONSTANT    PAYMENT_REQ = 402               ! Payment Required
CONSTANT    FORBIDDEN = 403                 ! Forbidden
CONSTANT    NOT_FOUND = 404                 ! Not Found
CONSTANT    NOT_ALLOWED = 405               ! Method Not Allowed
CONSTANT    NOT_ACCEPTABLE = 406            ! Not Acceptable
CONSTANT    PROXY_AUTH_REQ = 407            ! Proxy Authentication Required
CONSTANT    REQ_TIMEOUT = 408               ! Request Time-out
CONSTANT    CONFLICT = 409                  ! Conflict
CONSTANT    GONE = 410                      ! Gone
CONSTANT    LENGTH_REQ = 411                ! Length Required
CONSTANT    PRECOND_FAILED = 412            ! Precondition Failed
CONSTANT    REQ_ENTITY_2BIG = 413           ! Request Entity Too Large
CONSTANT    REQ_URI_2BIG = 414              ! Request-URI Too Large
CONSTANT    UNSUP_MEDIA = 415               ! Unsupported Media Type	
CONSTANT    OUT_OF_RANGE = 416              ! Requested range not satisfied
CONSTANT    EXPECT_FAILED = 417             ! Expectation Failed
CONSTANT    SERVER_ERROR = 500              ! Internal Server Error
CONSTANT    NOT_IMPLEMENTED = 501           ! Not Implemented
CONSTANT    BAD_GATEWAY = 502               ! Bad Gateway
CONSTANT    NO_SERVICE = 503                ! Service Unavailable
CONSTANT    GATEWAY_TIMEOUT = 504           ! Gateway Time-out
CONSTANT    UNSUP_HTTP_VER = 505            ! HTTP Version not supported

CONSTANT    REQ_SUCCEEDED = 1               ! Successful statement
CONSTANT    REQ_HTTP_CODE = 0               ! HTTP request succeeded.
CONSTANT    REQ_GENERIC_ERR = -1            ! Generic Error
CONSTANT    REQ_SOCKET_ERR = -2             ! Socket Error

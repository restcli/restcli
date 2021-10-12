package uos.dev.restcli

import uos.dev.restcli.configs.*

enum class ConfigDecorator {
    NOOP,
    THREE_STAR,
    FULL_STAR,
    MIDDLE_STAR
}

fun ConfigDecorator.toPrivateConfigDecorator(): PrivateConfigDecorator = when (this) {
    ConfigDecorator.NOOP -> NoopConfigDecorator
    ConfigDecorator.THREE_STAR -> ThreeStarConfigDecorator
    ConfigDecorator.FULL_STAR -> StarConfigDecorator
    ConfigDecorator.MIDDLE_STAR -> MiddleStarConfigDecorator
}

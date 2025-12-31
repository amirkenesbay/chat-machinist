package kz.rmr.chatmachinist.model

enum class BotCommandScope {
    DEFAULT,
    PRIVATE_CHAT,
    GROUP_CHAT,
    ADMINISTRATORS_ALL_CHAT,
    ADMINISTRATORS_CHAT,
    MEMBER_CHAT,
    CHAT
}
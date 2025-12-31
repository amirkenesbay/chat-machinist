package kz.rmr.chatmachinist.exception

import kz.rmr.chatmachinist.model.MatchedTransition
import java.lang.RuntimeException

class MatchedTransitionException(val matchedTransition: MatchedTransition<*, *>, cause: Throwable) : RuntimeException(cause) {
}
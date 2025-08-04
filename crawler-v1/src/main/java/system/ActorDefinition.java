package system;

import akka.actor.typed.ActorRef;

public record ActorDefinition(

    String actorId,

    ActorRef<?> actor,
    String spawnTrackerId,
    String guardianId
) {

}

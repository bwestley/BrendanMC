package us.westley.brendan.BrendanMC;

@FunctionalInterface
public interface SignCompleteHandler {
    void onSignClose(SignCompletedEvent event);
}
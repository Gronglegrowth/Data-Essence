package EsetKalenko.Halcyon.api.block;

/**
 * For methods common to all machines that interact with Halcyon's redstone monitoring components/related signal emitters.
 */
public interface RedstoneMonitorable {

    /**
     * Does this machine have work to do? e.g. is it actively using Tidal Energy?
     * @return whether an Overseeing Anchor will emit redstone based on machine's needs/status
     */
    boolean hasWork();
}

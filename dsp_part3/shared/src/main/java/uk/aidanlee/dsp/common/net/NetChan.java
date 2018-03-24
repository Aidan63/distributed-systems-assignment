package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.net.commands.*;

import java.util.*;

public class NetChan {

    private static final int SNAPSHOT_BACKUP = 32;
    private static final int MAX_PACKET_CMDS = 4;
    private static final int MAX_RELIABLE_CMDS = 4;

    /**
     * The sequence number of the next outgoing packet.
     */
    private int sequence;

    /**
     * The sequence number of the last packet received.
     */
    private int ackSequence;

    /**
     * The location this net channel sends data to.
     */
    private EndPoint destination;

    /**
     * Sliding window of states for this client.
     */
    private LinkedList<SnapshotStorage> snapshots;

    /**
     *
     */
    private SnapshotStorage dummySnapshot;

    /**
     * All of the reliable commands which need to be sent.
     */
    private LinkedList<Command> reliableCommandQueue;

    /**
     * All of the unreliable commands which need to be sent.
     */
    private LinkedList<Command> unreliableCommandQueue;

    /**
     * The reliable commands which are currently being sent.
     */
    private LinkedList<Command> reliableBuffer;

    /**
     *
     */
    public NetChan(EndPoint _dest) {
        sequence    = 0;
        ackSequence = 0;
        destination = _dest;

        snapshots     = new LinkedList<>();
        dummySnapshot = new SnapshotStorage(null, 0);

        reliableCommandQueue   = new LinkedList<>();
        unreliableCommandQueue = new LinkedList<>();
        reliableBuffer = new LinkedList<>();
    }

    // Getters and Setters

    public EndPoint getDestination() {
        return destination;
    }

    // Public API

    /**
     * Adds a reliable command to be sent through this net channel.
     * @param _c Command to add.
     */
    public void addReliableCommand(Command _c) {
        reliableCommandQueue.add(_c);
    }

    /**
     * Adds an unreliable command to be sent through this net channel.
     * @param _c Command to add.
     */
    public void addCommand(Command _c) {
        unreliableCommandQueue.add(_c);
    }

    /**
     * Add a snapshot into the queue of latest snapshots and create a delta diffed snapshot command.
     * @param _snapshot Latest snapshot.
     */
    public void addSnapshot(Snapshot _snapshot) {
        // Add the snapshot
        snapshots.addFirst(new SnapshotStorage(_snapshot, sequence));
        if (snapshots.size() > SNAPSHOT_BACKUP) {
            snapshots.removeLast();
        }

        // Add a delta compressed
        SnapshotStorage master  = snapshots.getFirst();
        SnapshotStorage lastAck = snapshots.stream().anyMatch(s -> s.ack) ? snapshots.stream().filter(s -> s.ack).findFirst().get() : dummySnapshot;
        addCommand(new CmdSnapshot(master.snapshot, lastAck.snapshot));
    }

    /**
     * Receives a series of bytes in a packet and read them into their individual commands.
     * @param _packet Packet containing the received byte data.
     * @return Array of commands.
     */
    public Command[] receive(Packet _packet) {

        // Read sequence and ACK numbers.
        int inSeq = _packet.getData().readInteger();
        int inAck = _packet.getData().readInteger();
        int sentT = _packet.getData().readInteger();

        // Set our outgoing ACK to the sequence number just received.
        ackSequence = inSeq;

        // If the MSB in the received ACK is "1" then our reliable buffer was received.
        // In which case we can clear the buffer and be ready to send more reliable commands.
        if (getMSB(inAck)) {
            reliableBuffer.clear();
        }

        // Read the commands from the packet.
        int numCmds = _packet.getData().readByte();
        Command[] cmds = new Command[numCmds];

        // Read all of the commands from the packet bytes.
        for (int i = 0; i < numCmds; i++) {
            byte cmdID = _packet.getData().readByte();
            switch (cmdID) {
                case Command.CLIENT_CONNECTED:
                    cmds[i] = new CmdClientConnected(_packet, sentT);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    cmds[i] = new CmdClientDisconnected(_packet, sentT);
                    break;

                case Command.CHAT_MESSAGE:
                    cmds[i] = new CmdChatMessage(_packet, sentT);
                    break;

                case Command.CLIENT_SETTINGS:
                    cmds[i] = new CmdClientSettings(_packet, sentT);
                    break;

                case Command.CLIENT_INPUT:
                    cmds[i] = new CmdClientInput(_packet, sentT);
                    break;

                case Command.SNAPSHOT:
                    cmds[i] = new CmdSnapshot(_packet, sentT);
                    break;

                case Command.SERVER_STATE:
                    cmds[i] = new CmdServerState(_packet, sentT);
                    break;
            }
        }

        return cmds;
    }

    /**
     * Builds a net chan update packet for sending over the network.
     * @return Packet with commands written into it.
     */
    public Packet send() {

        int numReliableToSend   = Math.min(reliableCommandQueue.size(), MAX_RELIABLE_CMDS);
        int numUnreliableToSend = Math.min(unreliableCommandQueue.size(), MAX_PACKET_CMDS);

        // Fill the reliable buffer if needed / possible.
        if (reliableBuffer.isEmpty() && numReliableToSend > 0) {
            for (int i = 0; i < numReliableToSend; i++) {
                reliableBuffer.add(reliableCommandQueue.removeFirst());
            }
        }

        // Create the header data.
        Packet packet = createNetChanPacket(numUnreliableToSend + reliableBuffer.size());

        // Add all of the commands
        for (Command cmd : reliableBuffer) {
            cmd.add(packet);
        }

        // Pop and add some unreliable commands.
        for (int i = 0; i < numUnreliableToSend; i++) {
            unreliableCommandQueue.remove().add(packet);
        }

        // Increase the sequence number.
        sequence++;

        return packet;
    }

    // Internal Functions

    /**
     * Creates a new packet with a net channel header.
     * @return Packet with net chan header.
     */
    private Packet createNetChanPacket(int _numCmds) {
        Packet packet = new Packet(destination);

        // Not an OOB packet
        packet.getData().writeBoolean(false);

        // Write 32 bits for the sequence number.
        // MSB is used to indicate if there are reliable commands and used for acknowledging them.
        packet.getData().writeInteger(setMSB(sequence, reliableBuffer.size() > 0));

        // Write 32 bits for the ACK sequence number
        packet.getData().writeInteger(ackSequence);

        // Write the current system time.
        packet.getData().writeInteger((int) System.currentTimeMillis());

        // Write a byte for the number of commands in this net chan packet.
        packet.getData().writeByte((byte) _numCmds);

        return packet;
    }

    /**
     *
     * @param _master
     * @param _latestAck
     * @param _packet
     */
    private void deltaCompressSnapshot(SnapshotStorage _master, SnapshotStorage _latestAck, Packet _packet) {
        //
    }

    /**
     * Gets the value of the most significant bit of an integer.
     * @param _val The integer to read.
     * @return Boolean representing the MSB value.
     */
    private boolean getMSB(int _val) {
        int mask = 1 << 32;
        return (_val & mask) > 0;
    }

    /**
     * Set the value of the most significant bit of an integer.
     * @param _val The integer to write.
     * @param _bit The new value of the MSB.
     * @return Integer
     */
    private int setMSB(int _val, boolean _bit) {
        if (_bit) {
            int mask = 1 << 32;
            return _val | mask;
        }

        int mask = 1 << 32;
        return _val & ~0x7FFFFFF;
    }

    /**
     *
     */
    private class SnapshotStorage {
        /**
         * Snapshot with all players data inside.
         */
        private Snapshot snapshot;

        /**
         * The netchan sequence this snapshot was sent out at.
         */
        private int sequence;

        /**
         * If this snapshot has been acknowledged by the client.
         */
        private boolean ack;

        /**
         *
         * @param _snap
         * @param _seq
         */
        private SnapshotStorage(Snapshot _snap, int _seq) {
            snapshot = _snap;
            sequence = _seq;
            ack = false;
        }
    }
}

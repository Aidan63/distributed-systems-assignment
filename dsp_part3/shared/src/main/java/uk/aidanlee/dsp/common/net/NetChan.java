package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.net.commands.*;

import java.util.*;

public class NetChan {

    private static final int PACKET_BACKUP = 32;
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
    private State[] states;

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

        states = new State[PACKET_BACKUP];

        reliableCommandQueue   = new LinkedList<>();
        unreliableCommandQueue = new LinkedList<>();
        reliableBuffer = new LinkedList<>();
    }

    // Getters and Setters

    public EndPoint getDestination() {
        return destination;
    }
    public void setDestination(EndPoint destination) {
        this.destination = destination;
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
     *
     * @param _packet
     * @return
     */
    public Command[] receive(Packet _packet) {

        // Read sequence and ACK numbers.
        int inSeq = _packet.getData().readInteger();
        int inAck = _packet.getData().readInteger();

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

        for (int i = 0; i < numCmds; i++) {
            byte cmdID = _packet.getData().readByte();
            switch (cmdID) {
                case Command.CLIENT_CONNECTED:
                    System.out.println("NetChan Command : Client Connected");
                    cmds[i] = new CmdClientConnected(_packet);
                    break;

                case Command.CLIENT_DISCONNECTED:
                    System.out.println("NetChan Command : Client Disconnected");
                    cmds[i] = new CmdClientDisconnected(_packet);
                    break;

                case Command.CHAT_MESSAGE:
                    System.out.println("NetChan Command : Chat Message");
                    cmds[i] = new CmdChatMessage(_packet);
                    break;

                case Command.CLIENT_UPDATED:
                    System.out.println("NetChan Command : Client Updated");
                    cmds[i] = new CmdClientUpdated(_packet);
                    break;
            }
        }

        return cmds;
    }

    /**
     * Builds a net chan update packet for sending over the network.
     * @return
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

        // Write a byte for the number of commands in this net chan packet.
        packet.getData().writeByte((byte) _numCmds);

        return packet;
    }

    private boolean getMSB(int _val) {
        int mask = 1 << 32;
        return (_val & mask) > 0;
    }

    private int setMSB(int _val, boolean _bit) {
        if (_bit) {
            int mask = 1 << 32;
            return _val | mask;
        }

        int mask = 1 << 32;
        return _val & ~0x7FFFFFF;
    }
}

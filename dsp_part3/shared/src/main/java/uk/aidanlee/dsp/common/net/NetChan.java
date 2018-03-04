package uk.aidanlee.dsp.common.net;

import uk.aidanlee.dsp.common.net.commands.Command;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
    private Queue<Command> reliableCommandQueue;

    /**
     * All of the unreliable commands which need to be sent.
     */
    private Queue<Command> unreliableCommandQueue;

    /**
     * The reliable commands which are currently being sent.
     */
    private List<Command> reliableBuffer;

    /**
     *
     */
    public NetChan(EndPoint _dest) {
        sequence    = 0;
        ackSequence = 0;

        destination = _dest;

        states = new State[PACKET_BACKUP];

        reliableCommandQueue   = new PriorityQueue<>();
        unreliableCommandQueue = new PriorityQueue<>();
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
     * Builds a net chan update packet for sending over the network.
     * @return
     */
    public byte[] send() {

        int numReliableToSend   = Math.min(reliableCommandQueue.size(), MAX_RELIABLE_CMDS);
        int numUnreliableToSend = Math.min(unreliableCommandQueue.size(), MAX_PACKET_CMDS);

        //if (numUnreliableToSend == 0 && (numReliableToSend == 0 || reliableBuffer.size() == 0 )) return null;

        // Fill the reliable buffer if needed / possible.
        if (reliableBuffer.isEmpty() && numReliableToSend > 0) {
            for (int i = 0; i < numReliableToSend; i++) {
                reliableBuffer.add(reliableCommandQueue.remove());
            }
        }

        // Create the header data.
        BitPacker data = createNetChanHeader();

        // Add our unreliable commands
        for (Command cmd : reliableBuffer) {
            cmd.add(data);
        }

        // Pop and add some unreliable commands.
        for (int i = 0; i < numUnreliableToSend; i++) {
            unreliableCommandQueue.remove().add(data);
        }

        // Increase the sequence number.
        sequence++;

        return data.toBytes();
    }

    /**
     *
     * @param _packet
     * @return
     */
    public BitPacker receive(BitPacker _packet) {
        System.out.println("Netchan message received");

        int inSeq = _packet.readInteger();
        int inAck = _packet.readInteger();

        if (getMSB(inAck)) {
            System.out.println("Reliable Ack'd");
            reliableBuffer.clear();
        }

        ackSequence = inSeq;

        return _packet;
    }

    // Internal Functions

    /**
     * Construct the net chan header for the next outgoing packet.
     * @return Bit packer with the header data written to it.
     */
    private BitPacker createNetChanHeader() {
        BitPacker header = new BitPacker();

        // Not an OOB packet
        header.writeBoolean(false);

        // Write 32 bits for the sequence number.
        // MSB is used to indicate if there are reliable commands and used for acknowledging them.
        sequence = setMSB(sequence, reliableBuffer.size() > 0);
        header.writeInteger(sequence);

        // Write 32 bits for the ACK sequence number
        header.writeInteger(ackSequence);

        // Write a byte for the number of commands in this net chan packet.
        header.writeByte((byte) (unreliableCommandQueue.size() + reliableBuffer.size()));

        return header;
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

package com.example.game2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

public class ClientMessages {
	public static final short CLIENT_MESSAGE_HIT = 0;
	public static final int	CLIENT_FLAG_COUNT = CLIENT_MESSAGE_HIT + 1;

	public static class HitClientMessage extends ClientMessage{

		public static final int COLOR_RED = 0;
		public static final int COLOR_GREEN = 1;
		public static final int COLOR_BLUE = 2;
		
		private int mID;
		private float mX;
		private float mY;
		private int mColorId;
		
		// Empty constructor needed for message pool allocation
		public HitClientMessage(){
			// Do nothing...
		}
		
		// Constructor
		public HitClientMessage(final int pID, final float pX, final float pY, final int pColorId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mColorId = pColorId;
		}
		
		// A Setter is needed to change values when we obtain a message from the message pool
		public void set(final int pID, final float pX, final float pY, final int pColorId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mColorId = pColorId;
		}
		
		// Getters
		public int getID(){
			return this.mID;
		}
		public float getX(){
			return this.mX;
		}
		public float getY(){
			return this.mY;
		}
		public int getColorId(){
			return this.mColorId;
		}
		
		// Get the message flag
		@Override
		public short getFlag() {
			return CLIENT_MESSAGE_HIT;
		}

		// Apply the read data to the message's member variables
		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
			this.mID = pDataInputStream.readInt();
			this.mX = pDataInputStream.readFloat();
			this.mY = pDataInputStream. readFloat();
			this.mColorId = pDataInputStream.readInt();
		}

		// Write the message's member variables to the output stream
		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
			pDataOutputStream.writeInt(mColorId);
		}
	}

}

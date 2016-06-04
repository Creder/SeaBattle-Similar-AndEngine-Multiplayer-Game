package com.example.game2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

public class ClientMessages {
	public static final short CLIENT_MESSAGE_PALUB = 0;
	public static final short CLIENT_MESSAGE_HIT = CLIENT_MESSAGE_PALUB+1;
	public static final int	CLIENT_FLAG_COUNT = CLIENT_MESSAGE_HIT + 1;

	public static class HitClientMessage extends ClientMessage{

		public static final int COLOR_RED = 0;
		public static final int COLOR_BLUE = 1;
		
		private int mID;
		private float mX;
		private float mY;
		private int mColorId;
		private int mFieldId;
		
		// Empty constructor needed for message pool allocation
		public HitClientMessage(){
			// Do nothing...
		}
		
		// Constructor
		public HitClientMessage(final int pID, final float pX, final float pY, final int pColorId, final int pFieldId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mColorId = pColorId;
			this.mFieldId = pFieldId;
		}
		
		// A Setter is needed to change values when we obtain a message from the message pool
		public void set(final int pID, final float pX, final float pY, final int pColorId, final int pFieldId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mColorId = pColorId;
			this.mFieldId = pFieldId;
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
		public int getFieldId(){
			return this.mFieldId;
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
			this.mFieldId = pDataInputStream.readInt();
		}

		// Write the message's member variables to the output stream
		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
			pDataOutputStream.writeInt(mColorId);
			pDataOutputStream.writeInt(mFieldId);
		}
	}

	public static class PalubClientMessage extends ClientMessage{
	
		private int mID;
		private float mX;
		private float mY;
		private int mFieldId;
		
		// Empty constructor needed for message pool allocation
		public PalubClientMessage(){
			// Do nothing...
		}
		
		// Constructor
		public PalubClientMessage(final int pID, final float pX, final float pY, final int pFieldId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mFieldId = pFieldId;
		}
		
		// A Setter is needed to change values when we obtain a message from the message pool
		public void set(final int pID, final float pX, final float pY, final int pFieldId){
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
			this.mFieldId = pFieldId;
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
		public int getFieldId(){
			return this.mFieldId;
		}
		
		// Get the message flag
		@Override
		public short getFlag() {
			return CLIENT_MESSAGE_PALUB;
		}

		// Apply the read data to the message's member variables
		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
			this.mID = pDataInputStream.readInt();
			this.mX = pDataInputStream.readFloat();
			this.mY = pDataInputStream. readFloat();
			this.mFieldId = pDataInputStream.readInt();
		}

		// Write the message's member variables to the output stream
		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
			pDataOutputStream.writeInt(mFieldId);
		}
	}
}

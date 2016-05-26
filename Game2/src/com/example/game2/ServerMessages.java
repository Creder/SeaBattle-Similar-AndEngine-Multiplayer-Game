package com.example.game2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class ServerMessages {
public static final short SERVER_MESSAGE_HIT = ClientMessages.CLIENT_FLAG_COUNT;
	
public static class HitServerMessage extends ServerMessage{

	private int mID;
	private float mX;
	private float mY;
	private int mColorId;
	
	public HitServerMessage(){
		// Do nothing...
	}
	
	public HitServerMessage(final int pID, final float pX, final float pY, final int pColorId){
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
		this.mColorId = pColorId;
	}
	
	public void set(final int pID, final float pX, final float pY, final int pColorId){
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
		this.mColorId = pColorId;
	}
	
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
	
	@Override
	public short getFlag() {
		return SERVER_MESSAGE_HIT;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mID = pDataInputStream.readInt();
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream. readFloat();
		this.mColorId = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(
			DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mID);
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
		pDataOutputStream.writeInt(this.mColorId);
	}
}

}

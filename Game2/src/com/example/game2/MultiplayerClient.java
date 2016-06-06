package com.example.game2;

import java.io.IOException;
import java.net.Socket;

import org.andengine.engine.Engine;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.color.Color;

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;
import com.example.game2.ServerMessages.UnitServerMessage;

import android.util.Log;

public class MultiplayerClient implements ISocketConnectionServerConnectorListener, GameConstants {

	private static final String TAG = "CLIENT";
	private Engine mEngine;
	private Rectangle mLeftField;
	private Rectangle mRightField;
	private Scene mScene;

	
	private String mServerIP;
	private int mServerPort;
	
	private ServerConnector<SocketConnection> mServerConnector;
	
	private int mColorId;
	
	public MultiplayerClient(final String pServerIP, final int pServerPort, final Engine pEngine, final Rectangle pLeftField, final Rectangle pRightField, final Scene pScene){
		this.mServerIP = pServerIP;
		this.mServerPort = pServerPort;
		this.mEngine = pEngine;
		this.mLeftField = pLeftField;
		this.mRightField = pRightField;
		this.mScene = pScene;
	}
	
	public void initClient(){
		this.mEngine.runOnUpdateThread(new Runnable(){
	
			@Override
			public void run() {
				try {
					
					Socket socket = new Socket(MultiplayerClient.this.mServerIP, MultiplayerClient.this.mServerPort);
					SocketConnection socketConnection = new SocketConnection(socket);

					MultiplayerClient.this.mServerConnector = new SocketConnectionServerConnector(socketConnection, MultiplayerClient.this);
					
					MultiplayerClient.this.mServerConnector.registerServerMessage(ServerMessages.SERVER_MESSAGE_HIT, HitServerMessage.class, new IServerMessageHandler<SocketConnection>(){

						@Override
						public void onHandleMessage(ServerConnector<SocketConnection> pServerConnector,	IServerMessage pServerMessage)
								throws IOException 
						{
							
							HitServerMessage message = (HitServerMessage) pServerMessage;

							Sprite hit = new Sprite(message.getX(), message.getY(), MainActivity.MissTextureRegion, mEngine.getVertexBufferObjectManager());
							hit.setWidth(CELLSIZE);
							hit.setHeight(CELLSIZE);
							
							final int colorId = message.getColorId();
							final int fieldId = message.getFieldId();

							switch(colorId){
							case HitClientMessage.COLOR_RED:
								hit.setColor(1,0,0);
								break;
							case HitClientMessage.COLOR_BLUE:
								hit.setColor(0,0,1);
								break;
							}
							
							switch(fieldId)
							{
								case FIELDLEFT_ID:
									mLeftField.attachChild(hit);
									break;
								case FIELDRIGHT_ID:
									mRightField.attachChild(hit);
									break;
							}
						}	
					});
					
					MultiplayerClient.this.mServerConnector.registerServerMessage(ServerMessages.SERVER_MESSAGE_UNIT, UnitServerMessage.class, new IServerMessageHandler<SocketConnection>(){

						@Override
						public void onHandleMessage(ServerConnector<SocketConnection> pServerConnector,	IServerMessage pServerMessage)
								throws IOException 
						{
							UnitServerMessage unitmessage = (UnitServerMessage) pServerMessage;

							Sprite unit = new Sprite(unitmessage.getX(), unitmessage.getY(),MainActivity.UnitTextureRegion, mEngine.getVertexBufferObjectManager()){
							    @Override
							    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
							    {
							    	if (pSceneTouchEvent.isActionDown())
							        {
							        	if(MainActivity.mServer != null)
							        	{
							        		if(MainActivity.mClient != null)
							        		{
							        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_X;
									    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_Y;
									            HitServerMessage message = (HitServerMessage) MainActivity.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
												message.set(SERVER_ID, x, y, 0, 1);
												MainActivity.mServer.sendMessage(message);
												MainActivity.mMessagePool.recycleMessage(message);
												return true;
							        		}
							        		
							        	}
							        	 else if(MainActivity.mClient != null)
							        	 {
									        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_X;
									    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_Y;
												HitClientMessage message = (HitClientMessage) MainActivity.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
												message.set(CLIENT_ID, x, y, 0, 0);
												MainActivity.mClient.sendMessage(message);
												MainActivity.mMessagePool.recycleMessage(message);
									
												return true;
										}	
							        }
							        return true;
							    }
							       
						};
							unit.setWidth(CELLSIZE);
							unit.setHeight(CELLSIZE);
							final int fieldId = unitmessage.getFieldId();
							unit.setColor(Color.GREEN);
							
							switch(fieldId)
							{
								case FIELDLEFT_ID:
									mLeftField.attachChild(unit);
									mScene.registerTouchArea(unit);
									break;
								case FIELDRIGHT_ID:
									mRightField.attachChild(unit);
									mScene.registerTouchArea(unit);
									break;
							}
						}
					});
					
					
					
					MultiplayerClient.this.mServerConnector.getConnection().start();
				} catch (IOException e) 
					{
						e.printStackTrace();
					}
			}
		});
	}
	
	public void setDrawColor(final int pColorId){
		this.mColorId = pColorId;
	}

	public int getDrawColor(){
		return this.mColorId;
	}	
	
	public void sendMessage(ClientMessage pClientMessage){
		try {
			this.mServerConnector.sendClientMessage(pClientMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "Sended");
	}

	public void terminate(){
		if(this.mServerConnector != null)
		this.mServerConnector.terminate();
	}

	@Override
	public void onStarted(ServerConnector<SocketConnection> pServerConnector) {
		Log.i(TAG, "Connected :" + pServerConnector.getConnection().getSocket().getInetAddress().getHostAddress().toString());
	}
	
	@Override
	public void onTerminated(ServerConnector<SocketConnection> pServerConnector) {
		Log.i(TAG, "Disonnected :" + pServerConnector.getConnection().getSocket().getInetAddress().getHostAddress().toString());
	}
}

package com.example.game2;

import java.io.IOException;
import java.net.Socket;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
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

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;


import android.util.Log;

public class MultiplayerClient implements ISocketConnectionServerConnectorListener {

	private static final String TAG = "CLIENT";
	private Engine mEngine;
	private Scene mScene;
	private Camera mCamera;
	private static int _CameraW;
	
	private String mServerIP;
	private int mServerPort;
	
	private ServerConnector<SocketConnection> mServerConnector;
	
	private int mColorId = HitClientMessage.COLOR_RED;
	
	public MultiplayerClient(final String pServerIP, final int pServerPort, final Engine pEngine, final Scene pScene, final Camera pCamera){
		this.mServerIP = pServerIP;
		this.mServerPort = pServerPort;
		this.mCamera = pCamera;
		this.mEngine = pEngine;
		this.mScene = pScene;
		_CameraW = (int) this.mCamera.getWidth();
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
								throws IOException {
							
							HitServerMessage message = (HitServerMessage) pServerMessage;
							
							final float cellSize = (_CameraW/3)/10;
							
							Sprite hit = new Sprite(message.getX(), message.getY(), MainActivity.MissTextureRegion, mEngine.getVertexBufferObjectManager());
							final int colorId = message.getColorId();

							// Set the point's color based on the message's color id
							switch(colorId){
							case HitClientMessage.COLOR_RED:
								hit.setColor(1,0,0);
								break;
							case HitClientMessage.COLOR_GREEN:
								hit.setColor(0,1,0);
								break;
							case HitClientMessage.COLOR_BLUE:
								hit.setColor(0,0,1);
								break;
							}
							mScene.attachChild(hit);
						}
						
					});
					
					MultiplayerClient.this.mServerConnector.getConnection().start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void setDrawColor(final int pColorId){
		this.mColorId = pColorId;
	}
	
	// Obtain color Id
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

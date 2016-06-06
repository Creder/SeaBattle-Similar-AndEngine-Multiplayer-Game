package com.example.game2;

import java.io.IOException;

import org.andengine.engine.Engine;

import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ClientMessages.UnitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;
import com.example.game2.ServerMessages.UnitServerMessage;

import android.util.Log;

public class MultiplayerServer implements 
		ISocketServerListener<SocketConnectionClientConnector>,
		ISocketConnectionClientConnectorListener  
	{
	
	private static final String TAG = "SERVER";
	private Engine mEngine;
	
	private int mServerPort;

	private SocketServer<SocketConnectionClientConnector> mSocketServer;

	public MultiplayerServer(final int pServerPort, final Engine pEngine) {
		this.mServerPort = pServerPort;
		this.mEngine = pEngine;
	}

	public void initServer() {
		
		this.mEngine.runOnUpdateThread(new Runnable()
		{

			@Override
			public void run() 
			{
				MultiplayerServer.this.mSocketServer = new SocketServer<SocketConnectionClientConnector>(
				MultiplayerServer.this.mServerPort,
				MultiplayerServer.this, MultiplayerServer.this) {

					@Override
					protected SocketConnectionClientConnector newClientConnector(
							SocketConnection pSocketConnection)
							throws IOException {


						final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);

						clientConnector.registerClientMessage(ClientMessages.CLIENT_MESSAGE_HIT, HitClientMessage.class, new IClientMessageHandler<SocketConnection>(){

							@Override
							public void onHandleMessage(
									ClientConnector<SocketConnection> pClientConnector,
									IClientMessage pClientMessage)
									throws IOException {
								HitClientMessage incomingMessage = (HitClientMessage) pClientMessage;
								HitServerMessage outgoingMessage = new HitServerMessage(incomingMessage.getID(), incomingMessage.getX(), incomingMessage.getY(), incomingMessage.getColorId(), incomingMessage.getFieldId());
								sendMessage(outgoingMessage);
							}
						});
						
						clientConnector.registerClientMessage(ClientMessages.CLIENT_MESSAGE_UNIT, UnitClientMessage.class, new IClientMessageHandler<SocketConnection>(){

							@Override
							public void onHandleMessage(
									ClientConnector<SocketConnection> pClientConnector,
									IClientMessage pClientMessage)
									throws IOException {
								UnitClientMessage incomingUnit = (UnitClientMessage) pClientMessage;
								UnitServerMessage outgoingUnit = new UnitServerMessage(incomingUnit.getID(), incomingUnit.getX(), incomingUnit.getY(), incomingUnit.getFieldId());
								sendMessage(outgoingUnit);
							}
						});
						return clientConnector;
					}
				};
				MultiplayerServer.this.mSocketServer.start();
			}
			
		});

	}
	
	
	public void sendMessage(ServerMessage pServerMessage){
		try {
			this.mSocketServer.sendBroadcastServerMessage(pServerMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void terminate(){
		if(this.mSocketServer != null)
		this.mSocketServer.terminate();
	}

	@Override
	public void onStarted(ClientConnector<SocketConnection> pClientConnector) {
		Log.i(TAG, "Client Connected: "
				+ pClientConnector.getConnection().getSocket().getInetAddress()
						.getHostAddress());
	}
	@Override
	public void onTerminated(ClientConnector<SocketConnection> pClientConnector) {
		Log.i(TAG, "Client Disconnected: "
				+ pClientConnector.getConnection().getSocket().getInetAddress()
						.getHostAddress());
	}
	@Override
	public void onStarted(
			SocketServer<SocketConnectionClientConnector> pSocketServer) {
		Log.i(TAG, "Started");
	}
	@Override
	public void onTerminated(
			SocketServer<SocketConnectionClientConnector> pSocketServer) {
		Log.i(TAG, "Terminated");
	}
	@Override
	public void onException(
			SocketServer<SocketConnectionClientConnector> pSocketServer,
			Throwable pThrowable) {
		Log.i(TAG, "Exception: ", pThrowable);

	}
}

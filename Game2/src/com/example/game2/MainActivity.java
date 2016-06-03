package com.example.game2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RelativeResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;

import org.andengine.entity.scene.background.RepeatingSpriteBackground;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.ITextureRegion;

import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.EditText;



public class MainActivity extends SimpleBaseGameActivity implements GameConstants
{
	private static final int DIALOG_CHOOSE_ENVIRONMENT_ID = 0;
	private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_ENVIRONMENT_ID + 1;
	private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;

	private Camera mCamera;
	private Scene mScene;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private RepeatingSpriteBackground Background;
	private ITextureRegion UnitTextureRegion;
	public static ITextureRegion MissTextureRegion;
	private Music mMusic;

	private Rectangle fieldleft;
	private Rectangle fieldright;
	private Line line;
	
	private Rectangle[] palub;

	private String mServerIP = LOCALHOST_IP;

	final static MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

	private MultiplayerServer mServer;
	private MultiplayerClient mClient;
	
	private int palubcount = 10;


	@Override
	public EngineOptions onCreateEngineOptions() {
		this.showDialog(DIALOG_CHOOSE_ENVIRONMENT_ID);
		
		this.mMessagePool.registerMessage(ServerMessages.SERVER_MESSAGE_HIT, HitServerMessage.class);
		this.mMessagePool.registerMessage(ClientMessages.CLIENT_MESSAGE_HIT, HitClientMessage.class);
		
		this.mCamera = new Camera(0, 0, CAMERAW, CAMERAH);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RelativeResolutionPolicy(1, 1), this.mCamera);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		
		return engineOptions; 
	}


	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("Graphics/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(getTextureManager(), CAMERAW, CAMERAH, TextureOptions.BILINEAR);
		this.UnitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "Gamer.bmp", 0, 0);
		this.MissTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "miss.bmp", 0, 0);
		this.Background = new RepeatingSpriteBackground(CAMERAW, CAMERAH, getTextureManager(),
				AssetBitmapTextureAtlasSource.create(getAssets(), "Graphics/desertBackground.jpg"), getVertexBufferObjectManager());
		this.mBitmapTextureAtlas.load();
		
		MusicFactory.setAssetBasePath("Music/");
		try {
			this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "Turbulence.ogg");
			this.mMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
	
		mScene = new Scene();
		mScene.setBackground(this.Background);
		//mMusic.play();
		palub = new Rectangle[palubcount];
		Rectangle generator = new Rectangle(0, 0, (CAMERAW/3)/10, (CAMERAW/3)/10, vertexBufferObjectManager)
				{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y)
				{
					if(pSceneTouchEvent.isActionDown())
					{
						Clearfield();
						for(int i = 0 ; i < palubcount ; i++)
						{				
							int x = (int) RandomPalub(104, (104+(int)fieldleft.getWidth())-((CAMERAW/3)/10));
							int y = (int) RandomPalub(104, (104+(int)fieldleft.getWidth())-((CAMERAW/3)/10));
							x = (int)((((int)x/ (int)CELLSIZE) * CELLSIZE));
							y = (int)((((int)y/ (int)CELLSIZE) * CELLSIZE));
							palub[i] = new Rectangle(x,y, CELLSIZE, CELLSIZE, mEngine.getVertexBufferObjectManager());		
							palub[i].setColor(Color.YELLOW);
							mScene.attachChild(palub[i]);
						}
					}
					return true;
				}
				};
				
		Rectangle sender = new Rectangle(0, 50, (CAMERAW/3)/10, (CAMERAW/3)/10, vertexBufferObjectManager)
				{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y)
				{
					if(pSceneTouchEvent.isActionDown())
					{

						if(mServer != null)
						{
			        		if(mClient != null)
			        		{
			        			Clearfield();
			        			for(int i = 0 ; i < palubcount ; i++){
			        			int x =	(int)palub[i].getX();
								int y = (int)palub[i].getY();
					            HitServerMessage message = (HitServerMessage) MainActivity.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
								message.set(SERVER_ID, x, y, 1);
								mServer.sendMessage(message);
								MainActivity.mMessagePool.recycleMessage(message);
			        			}
			        		}
			        		
			        	}
						
						else if(mClient != null)
			        	{
							Clearfield();
							for(int i = 0 ; i < palubcount ; i++){
							int x =	(int)palub[i].getX();
							int y = (int)palub[i].getY();
							HitClientMessage message = (HitClientMessage) MainActivity.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
							message.set(CLIENT_ID, x, y, 1);
							mClient.sendMessage(message);
							MainActivity.mMessagePool.recycleMessage(message);
							}
			        	}	
			        } 
					
					return true;
				}
				};	
		
		fieldleft = new Rectangle(104, 104, CAMERAW/3, CAMERAW/3, vertexBufferObjectManager)
		{
		    @Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
		    {
		    	if (pSceneTouchEvent.isActionDown())
		        {
		        	if(mServer != null)
		        	{
		        		if(mClient != null)
		        		{
		        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE));
				    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE));
				            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
							message.set(SERVER_ID, x, y, 2);
							mServer.sendMessage(message);
							MainActivity.this.mMessagePool.recycleMessage(message);
							
							return true;
		        		}
		        		
		        	}
		        } 
		        else if(mClient != null)
		        {
		        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE));
		    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE));
					HitClientMessage message = (HitClientMessage) MainActivity.this.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
					message.set(CLIENT_ID, x, y,2);
					mClient.sendMessage(message);
					MainActivity.this.mMessagePool.recycleMessage(message);
		
					return true;
				}	
		        	return true;
		    }
		    
		       
		 };


		fieldright = new Rectangle(442, 104, CAMERAW/3, CAMERAW/3, vertexBufferObjectManager)
		{
			    @Override
			    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			    {
			    	if (pSceneTouchEvent.isActionDown())
			        {
			        	if(mServer != null){
			        		if(mClient != null){
			        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE));
					    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE));
					            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
								message.set(SERVER_ID, x, y, 2);
								mServer.sendMessage(message);
								MainActivity.this.mMessagePool.recycleMessage(message);
							return true;
			        		}
			        		
			        	}
			        } else if(mClient != null){
			        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE));
			    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE));
						HitClientMessage message = (HitClientMessage) MainActivity.this.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
						message.set(CLIENT_ID, x, y, 2);
						mClient.sendMessage(message);
						MainActivity.this.mMessagePool.recycleMessage(message);
			
						return true;
					}	
			        return true;
			        }
			       
			    };
			    
		
		for (int col = 0; col < 11; col++) {
			line = new Line(CELLSIZE * col, 0,
					CELLSIZE * col, fieldleft.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			fieldleft.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					fieldleft.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			fieldleft.attachChild(line);
		}
		
		for (int col = 0; col < 11; col++) {
			line = new Line(CELLSIZE * col, 0,
					CELLSIZE * col, fieldright.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			fieldright.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					fieldright.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			fieldright.attachChild(line);
		}
		
		
		mScene.registerTouchArea(generator);
		mScene.attachChild(generator);
		mScene.registerTouchArea(sender);
		mScene.attachChild(sender);
		fieldleft.setColor(Color.TRANSPARENT);
		fieldright.setColor(Color.TRANSPARENT);
		mScene.registerTouchArea(fieldleft);
		mScene.registerTouchArea(fieldright);
		mScene.attachChild(fieldleft);
		mScene.attachChild(fieldright);
		
		

		return mScene;
	}
	
	private void Clearfield()
	{
		for(int i = 0; i < palubcount; i++)
		{
			mScene.detachChild(palub[i]);
			//palub[i].dispose();
		}
	}

	private static int RandomPalub(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
		
	@Override
	protected Dialog onCreateDialog(final int pID) {
		switch(pID) {
			case DIALOG_SHOW_SERVER_IP_ID:
				try {
					return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Your Server-IP ...")
					.setCancelable(false)
					.setMessage("The IP of your Server is:\n" + WifiUtils.getWifiIPv4Address(this))
					.setPositiveButton(android.R.string.ok, null)
					.create();
				} catch (final UnknownHostException e) {
					return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Your Server-IP ...")
					.setCancelable(false)
					.setMessage("Error retrieving IP of your Server: " + e)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog, final int pWhich) {
							MainActivity.this.finish();
						}
					})
					.create();
				}
			case DIALOG_CHOOSE_ENVIRONMENT_ID:
				return new AlertDialog.Builder(this)
						.setTitle("Choose server or client environment...")
						.setCancelable(false)
						.setPositiveButton("Client", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								MainActivity.this
										.showDialog(DIALOG_ENTER_SERVER_IP_ID);
							}

						})
						.setNegativeButton("Server and Client",
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										MainActivity.this
												.initServerAndClient();
										MainActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
									}

								}).create();

			case DIALOG_ENTER_SERVER_IP_ID:
				final EditText editText = new EditText(this);
				return new AlertDialog.Builder(this).setTitle("Enter Server IP...")
						.setCancelable(false).setView(editText)
						.setPositiveButton("Connect", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								

								MainActivity.this.mServerIP = editText
										.getText().toString();
	
								mClient = new MultiplayerClient(mServerIP,
										SERVER_PORT, mEngine, mScene, mCamera);

								mClient.initClient();
							}
						}).create();
			}
			return super.onCreateDialog(pID);
		
	}
	
	private void initServerAndClient() {
		mServer = new MultiplayerServer(SERVER_PORT, mEngine);
		mServer.initServer();


		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mClient = new MultiplayerClient(mServerIP, SERVER_PORT, mEngine, mScene, mCamera);
		mClient.initClient();
		
	}

	@Override
	protected void onDestroy() {
		if (this.mClient != null)
			this.mClient.terminate();

		if (this.mServer != null)
			this.mServer.terminate();
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		switch(pKeyCode) {
			case KeyEvent.KEYCODE_BACK:
				this.finish();
				return true;
		}
		return super.onKeyUp(pKeyCode, pEvent);
	}
}

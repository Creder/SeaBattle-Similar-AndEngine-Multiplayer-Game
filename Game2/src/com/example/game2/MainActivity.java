package com.example.game2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RelativeResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.widget.EditText;



public class MainActivity extends SimpleBaseGameActivity implements GameConstants
{
	private static final String LOCALHOST_IP = "127.0.0.1";
	private static final int SERVER_PORT = 4444;
	private static final int SERVER_ID = 0;
	private static final int CLIENT_ID = 1;
	private static final int CAMERAW = 780;
	private static final int CAMERAH = 420;

	private static final int DIALOG_CHOOSE_ENVIRONMENT_ID = 0;
	private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_ENVIRONMENT_ID + 1;
	private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;


	private Rectangle leftfield;
	private Rectangle rightfield;
	public Line line;
	
	private Music mMusic;

	

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private RepeatingSpriteBackground Background;
	private ITextureRegion UnitTextureRegion;
	public static ITextureRegion MissTextureRegion;
	private Camera mCamera;
	private Scene mScene;

	private String mServerIP = LOCALHOST_IP;

	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

	private MultiplayerServer mServer;
	private MultiplayerClient mClient;
	


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
		mMusic.play();

		leftfield = new Rectangle(FIELDLEFT_X, FIELDLEFT_Y, CAMERAW/3, CAMERAW/3, vertexBufferObjectManager)
		{
		    @Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
		    {
		    	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP)
		        {
		        	if(mServer != null)
		        	{
		        		if(mClient != null)
		        		{
		        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_X;
				    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_Y;
				            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
							message.set(SERVER_ID, x, y, 2, 0);
							mServer.sendMessage(message);
							
							MainActivity.this.mMessagePool.recycleMessage(message);
							
							return true;
		        		}
		        		
		        	}
		        	else if(mClient != null)
			        {
			        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_X;
			    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_Y;
						HitClientMessage message = (HitClientMessage) MainActivity.this.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
						message.set(CLIENT_ID, x, y, 2, 0);
						mClient.sendMessage(message);
						MainActivity.this.mMessagePool.recycleMessage(message);
			
						return true;
					}	
		        } 
		        
		        	return true;
		    }
		    
		       
		 };


		 rightfield = new Rectangle(FIELDRIGHT_X, FIELDRIGHT_Y, CAMERAW/3, CAMERAW/3, vertexBufferObjectManager)
		{
			    @Override
			    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			    {
			    	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP)
			        {
			        	if(mServer != null)
			        	{
			        		if(mClient != null)
			        		{
			        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_X;
					    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_Y;
					            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
								message.set(SERVER_ID, x, y, 2, 1);
								mServer.sendMessage(message);
								MainActivity.this.mMessagePool.recycleMessage(message);
								return true;
			        		}
			        		
			        	}
			        	 else if(mClient != null)
			        	 {
					        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_X;
					    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_Y;
								HitClientMessage message = (HitClientMessage) MainActivity.this.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
								message.set(CLIENT_ID, x, y, 2, 1);
								mClient.sendMessage(message);
								MainActivity.this.mMessagePool.recycleMessage(message);
					
								return true;
						}	
			        }
			        return true;
			    }
			       
		};

		
		for (int col = 0; col < 11; col++) {
			line = new Line(CELLSIZE * col, 0,
					CELLSIZE * col, leftfield.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			leftfield.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					leftfield.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			leftfield.attachChild(line);
		}
		
		for (int col = 0; col < 11; col++) {
			line = new Line(CELLSIZE * col, 0,
					CELLSIZE * col, rightfield.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			rightfield.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					rightfield.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLUE);
			rightfield.attachChild(line);
		}
		
		leftfield.setColor(Color.TRANSPARENT);
		rightfield.setColor(Color.TRANSPARENT);
		mScene.registerTouchArea(leftfield);
		mScene.registerTouchArea(rightfield);
		mScene.attachChild(leftfield);
		mScene.attachChild(rightfield);

		
		return mScene;
	}
	
	private int Rand(int min, int max)
	{
			Random random = new Random();

			int randomInt =  random.nextInt(max - min + 1) + min;
		      return randomInt;
		    
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
										SERVER_PORT, mEngine, leftfield,
								rightfield);

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

		mClient = new MultiplayerClient(mServerIP, SERVER_PORT, mEngine, leftfield, rightfield);
		mClient.initClient();
		
	}
	
	@Override
	protected void onDestroy() {
		if (this.mClient != null)
			this.mClient.terminate();

		if (this.mServer != null)
			this.mServer.terminate();
		mScene.detachSelf();
		mScene.dispose();
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
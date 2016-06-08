package com.example.game2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RelativeResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
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
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import com.example.game2.ClientMessages.HitClientMessage;
import com.example.game2.ClientMessages.UnitClientMessage;
import com.example.game2.ServerMessages.HitServerMessage;
import com.example.game2.ServerMessages.UnitServerMessage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.widget.EditText;



public class MainActivity extends SimpleBaseGameActivity implements GameConstants
{
	
	private static final int DIALOG_CHOOSE_ENVIRONMENT_ID = 0;
	private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_ENVIRONMENT_ID + 1;
	private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;


	private Rectangle _leftfield;
	private Rectangle _rightfield;
	private Rectangle[] unit;
	private Rectangle LeftGenerator;
	private Rectangle LeftSender;
	private Rectangle RightGenerator;
	private Rectangle RightSender;
	
	public Line line;
	
	private Music mMusic;

	

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private RepeatingSpriteBackground Background;
	public static ITextureRegion UnitTextureRegion;
	public static ITextureRegion MissTextureRegion;
	private Camera mCamera;
	private Scene mScene;
	private Sprite mUnit;

	private String mServerIP = LOCALHOST_IP;

	public final static MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

	static MultiplayerServer mServer;
	static MultiplayerClient mClient;
	


	@Override
	public EngineOptions onCreateEngineOptions() {
		this.showDialog(DIALOG_CHOOSE_ENVIRONMENT_ID);
		
		this.mMessagePool.registerMessage(ServerMessages.SERVER_MESSAGE_HIT, HitServerMessage.class);
		this.mMessagePool.registerMessage(ClientMessages.CLIENT_MESSAGE_HIT, HitClientMessage.class);
		this.mMessagePool.registerMessage(ServerMessages.SERVER_MESSAGE_UNIT, UnitServerMessage.class);
		this.mMessagePool.registerMessage(ClientMessages.CLIENT_MESSAGE_UNIT, UnitClientMessage.class);
		
		
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
		unit = new Rectangle[UNITCOUNT];
		for(int i = 0; i < UNITCOUNT; i++)
		{
			unit[i] = new Rectangle(0, 0, CELLSIZE, CELLSIZE, vertexBufferObjectManager);
		}

		LeftGenerator = new Rectangle(0, 0, CELLSIZE, CELLSIZE, vertexBufferObjectManager)
							{
							@Override
							public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y)
							{
								if(pSceneTouchEvent.isActionDown())
								{
										ClearField(_leftfield, unit);
										for(int i = 0 ; i < UNITCOUNT ; i++)
										{				
											int x = (int) Rand(0, ((int)_leftfield.getWidth())-((CAMERAW/3)/10));
											int y = (int) Rand(0, ((int)_leftfield.getWidth())-((CAMERAW/3)/10));
											x = (int)((((int)x/ (int)CELLSIZE) * CELLSIZE));
											y = (int)((((int)y/ (int)CELLSIZE) * CELLSIZE));
											unit[i].setX(x);
											unit[i].setY(y);
											unit[i].setColor(Color.YELLOW);
											_leftfield.attachChild(unit[i]);
										}
									}
									return true;
								}
								};
								
		LeftSender = new Rectangle(0, 50, CELLSIZE, CELLSIZE, vertexBufferObjectManager)
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
							        			ClearField(_leftfield, unit);
							        			for(int i = 0 ; i < UNITCOUNT ; i++){
							        			int x =	(int)unit[i].getX();
												int y = (int)unit[i].getY();
									            UnitServerMessage message = (UnitServerMessage) MainActivity.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_UNIT);
												message.set(SERVER_ID, x, y, 0);
												mServer.sendMessage(message);
												MainActivity.mMessagePool.recycleMessage(message);
							        			}
							        		}
							        		
							        	}
										
										else if(mClient != null)
							        	{
											ClearField(_leftfield, unit);
											for(int i = 0 ; i < UNITCOUNT ; i++){
											int x =	(int)unit[i].getX();
											int y = (int)unit[i].getY();
											UnitClientMessage message = (UnitClientMessage) MainActivity.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_UNIT);
											message.set(CLIENT_ID, x, y, 0);
											mClient.sendMessage(message);
											MainActivity.mMessagePool.recycleMessage(message);
											}
							        	}	
							        } 
									
									return true;
								}
								};
								
		/*RightGenerator = new Rectangle(CAMERAW-CELLSIZE, 0, CELLSIZE, CELLSIZE, vertexBufferObjectManager)
								{
								@Override
								public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y)
								{
									if(pSceneTouchEvent.isActionDown())
									{
											ClearField(_rightfield, unit);
											for(int i = 0 ; i < UNITCOUNT ; i++)
											{				
												int x = (int) Rand(0, ((int)_rightfield.getWidth())-((CAMERAW/3)/10));
												int y = (int) Rand(0, ((int)_rightfield.getWidth())-((CAMERAW/3)/10));
												x = (int)((((int)x/ (int)CELLSIZE) * CELLSIZE));
												y = (int)((((int)y/ (int)CELLSIZE) * CELLSIZE));
												unit[i].setX(x);
												unit[i].setY(y);
												unit[i].setColor(Color.YELLOW);
												_rightfield.attachChild(unit[i]);
											}
										}
										return true;
									}
									};
									
		RightSender = new Rectangle(CAMERAW-CELLSIZE, 50, CELLSIZE, CELLSIZE, vertexBufferObjectManager)
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
								        			ClearField(_rightfield, unit);
								        			for(int i = 0 ; i < UNITCOUNT ; i++){
								        			int x =	(int)unit[i].getX();
													int y = (int)unit[i].getY();
										            UnitServerMessage message = (UnitServerMessage) MainActivity.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_UNIT);
													message.set(SERVER_ID, x, y, 1);
													mServer.sendMessage(message);
													MainActivity.mMessagePool.recycleMessage(message);
								        			}
								        		}
								        		
								        	}
											
											if(mClient != null)
								        	{
												ClearField(_leftfield, unit);
												for(int i = 0 ; i < UNITCOUNT ; i++){
												int x =	(int)unit[i].getX();
												int y = (int)unit[i].getY();
												UnitClientMessage message = (UnitClientMessage) MainActivity.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_UNIT);
												message.set(CLIENT_ID, x, y, 1);
												mClient.sendMessage(message);
												MainActivity.mMessagePool.recycleMessage(message);
												}
								        	}	
								        } 
										
										return true;
									}
									};	*/
		_leftfield = new Rectangle(FIELDLEFT_X, FIELDLEFT_Y, FIELD_WIDTH, FIELD_WIDTH, vertexBufferObjectManager)
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
		        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_X;
				    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_Y;
				            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
							message.set(SERVER_ID, x, y, 1, 0);
							mServer.sendMessage(message);
							
							MainActivity.mMessagePool.recycleMessage(message);
							
							return true;
		        		}
		        		
		        	}
		        	else if(mClient != null)
			        {
			        	int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_X;
			    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDLEFT_Y;
						HitClientMessage message = (HitClientMessage) MainActivity.this.mMessagePool.obtainMessage(ClientMessages.CLIENT_MESSAGE_HIT);
						message.set(CLIENT_ID, x, y, 1, 0);
						mClient.sendMessage(message);
						MainActivity.this.mMessagePool.recycleMessage(message);
			
						return true;
					}	
		        } 
		        
		        	return true;
		    }   
		 };

		 _rightfield = new Rectangle(FIELDRIGHT_X, FIELDRIGHT_Y, FIELD_WIDTH, FIELD_WIDTH, vertexBufferObjectManager)
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
			        			int x = (int)((((int)pSceneTouchEvent.getX()/ (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_X;
					    		int y = (int)((((int)pSceneTouchEvent.getY() / (int)CELLSIZE) * CELLSIZE))-FIELDRIGHT_Y;
					            HitServerMessage message = (HitServerMessage) MainActivity.this.mMessagePool.obtainMessage(ServerMessages.SERVER_MESSAGE_HIT);
								message.set(SERVER_ID, x, y, 1, 1);
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
								message.set(CLIENT_ID, x, y, 1, 1);
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
					CELLSIZE * col, _leftfield.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLACK);
			_leftfield.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					_leftfield.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLACK);
			_leftfield.attachChild(line);
		}
		
		for (int col = 0; col < 11; col++) {
			line = new Line(CELLSIZE * col, 0,
					CELLSIZE * col, _rightfield.getHeight(), vertexBufferObjectManager);
			line.setColor(Color.BLACK);
			_rightfield.attachChild(line);
			
		}
		for (int row = 0; row < 11; row++) {
			line = new Line(0, CELLSIZE * row,
					_rightfield.getHeight(), CELLSIZE * row, vertexBufferObjectManager);
			line.setColor(Color.BLACK);
			_rightfield.attachChild(line);
		}

		
		_leftfield.setColor(Color.TRANSPARENT);
		_rightfield.setColor(Color.TRANSPARENT);
		mScene.attachChild(_leftfield);
		mScene.attachChild(_rightfield);
		
	
		return mScene;
	}
	
	private int Rand(int min, int max)
	{
			Random random = new Random();

			int randomInt =  random.nextInt(max - min + 1) + min;
		      return randomInt;
		    
	}
	
	private void ClearField(Rectangle pField, Rectangle[] pUnit)
	{
		for(int i = 0 ; i< UNITCOUNT ; i++)
		{
			pField.detachChild(pUnit[i]);
		}
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

								mScene.registerTouchArea(RightGenerator);
								mScene.registerTouchArea(RightSender);
								mScene.attachChild(RightGenerator);
								mScene.attachChild(RightSender);
								mScene.registerTouchArea(_leftfield);
								MainActivity.this
										.showDialog(DIALOG_ENTER_SERVER_IP_ID);
							}

						})
						.setNegativeButton("Server and Client",
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										mScene.registerTouchArea(LeftGenerator);
										mScene.registerTouchArea(LeftSender);
										mScene.attachChild(LeftGenerator);
										mScene.attachChild(LeftSender);
										mScene.registerTouchArea(_rightfield);
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
										SERVER_PORT, mEngine, _leftfield,
								_rightfield, mScene);

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

		mClient = new MultiplayerClient(mServerIP, SERVER_PORT, mEngine, _leftfield, _rightfield, mScene);
		mClient.initClient();
		
	}
	
	@Override
	protected void onDestroy() {
		if (this.mClient != null)
			this.mClient.terminate();

		if (this.mServer != null)
			this.mServer.terminate();
		//mScene.detachSelf();
		//mScene.dispose();
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
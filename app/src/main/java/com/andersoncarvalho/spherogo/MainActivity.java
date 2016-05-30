package com.andersoncarvalho.spherogo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;

/**
 * Created by anderson on 16/03/16.
 */

public class MainActivity extends AppCompatActivity
        implements RobotChangedStateListener, NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    public ConvenienceRobot mRobot;
    public DualStackDiscoveryAgent conexao;
    public TextView status;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private Camera mCamera;
    private CameraPreview mPreview;
    private FloatingActionButton goButton;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;


    private static float VELOCIDADE_SPHERO = 0.3f;
    private static int NUM_MOVIMENTOS = 6;
    ArrayList<Integer> listaMovimentos;

    ImageView setaesquerda, setadireita, setacima, setabaixo;

    private float bateria;

    boolean executandoMovimentos = false;

    ListView list;

    MovimentoList adapter;


    /**
     * Para controle do sphero podemos levar em consideracao o seguinte
     * 0 move ela para frente
     * 90 move ela para a direita
     * 180 move ela para tras
     * 270 move ela para a esquerda
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checarPermissoesDeLocalizacao()) {
            conexao.getInstance().addRobotStateListener(this);
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listaMovimentos = new ArrayList<Integer>();

        status = (TextView) findViewById(R.id.status);
        setaesquerda = (ImageView) findViewById(R.id.setaesquerda);
        setadireita = (ImageView) findViewById(R.id.setadireita);
        setabaixo = (ImageView) findViewById(R.id.setabaixo);
        setacima = (ImageView) findViewById(R.id.setacima);

        goButton = (FloatingActionButton) findViewById(R.id.gobotao);


        if(checarPermissoesDaCamera()) {
            mCamera = getCameraInstance();
            // Cria o preview da camera
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

        adapter = new
                MovimentoList(MainActivity.this, listaMovimentos);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "Você selecionou o movimento de número " + (position + 1), Toast.LENGTH_SHORT).show();

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sphero Go");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Movimente seu celular! Bateria do Sphero: " + (long) (bateria * 100) + "%", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        if (goButton != null) {
            goButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Executando movimentos ...", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    executarListaMovimentos();
                }
            });
        }

        FloatingActionButton configuracoes = (FloatingActionButton) findViewById(R.id.configs);
        if (configuracoes != null) {
            configuracoes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mostrarDialogoConfiguracoes();
                }
            });
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public boolean checarPermissoesDaCamera(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    public boolean checarPermissoesDeLocalizacao(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            return false;
        }
    }

    /** Metodo para pegar a instancia da camera */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // Tenta pegar a camera
        }
        catch (Exception e){
            // Camera deu pau ou nao existe
        }
        return c; // retorna nulo se a camera nao existe
    }

    protected void mostrarDialogoConfiguracoes() {

        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_config, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText velocidadeInput = (EditText) promptView.findViewById(R.id.velocidade);
        velocidadeInput.setText(((long) (VELOCIDADE_SPHERO * 100)) + "");

        final EditText numMovimentos = (EditText) promptView.findViewById(R.id.numMovimentos);
        numMovimentos.setText(NUM_MOVIMENTOS + "");

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        VELOCIDADE_SPHERO = (float) Long.parseLong(velocidadeInput.getText().toString()) / 100;
                        NUM_MOVIMENTOS = Integer.parseInt(numMovimentos.getText().toString());
                        Log.d("Velocidade Sphero", VELOCIDADE_SPHERO + "");
                    }
                })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarBuscaPorShero();
    }

    public void iniciarBuscaPorShero() {

        //Se o  DiscoveryAgent nao estiver procurando a sphero, ele ira comecar.
        if (!conexao.getInstance().isDiscovering()) {
            try {
                conexao.getInstance().startDiscovery(getApplicationContext());
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }
    }

    public void esconderSetas() {
        setadireita.setVisibility(View.INVISIBLE);
        setaesquerda.setVisibility(View.INVISIBLE);
        setacima.setVisibility(View.INVISIBLE);
        setabaixo.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu esta desativado
        //   getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Items do navigation drawer
        int id = item.getItemId();

        if (id == R.id.nav_connect) {
            iniciarBuscaPorShero();
        } else if (id == R.id.nav_disconnect) {
            if (mRobot != null) {
                mRobot.disconnect();
                mRobot = null;
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        mRobot = new ConvenienceRobot(robot);
        switch (type) {
            case Online: {
                status.setText("Status : Online");
                break;
            }
            case Connected: {
                status.setText("Status : Conectado");
                Log.d("Go!", "Sphero conectado");
                Toast.makeText(getApplicationContext(), "Sphero conectado!", Toast.LENGTH_SHORT).show();
            }
            case Disconnected: {
                status.setText("Status : Desconectado");
                Log.d("Go!", "Sphero desconectado");
            }
            case FailedConnect: {
                Log.d("Go!", "Falha ao conectar no Sphero");
            }
            case Connecting: {
                status.setText("Status : Conectando ...");
                Log.d("Go!", "Conectando ao Sphero ...");
                Toast.makeText(getApplicationContext(), "Conectando ao Sphero ...", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void executarListaMovimentos() {
        if (!listaMovimentos.isEmpty()) {
            executandoMovimentos = true;
            esconderSetas();

            switch (listaMovimentos.get(0)) {
                case R.mipmap.seta_esquerda:
                    setaesquerda.setVisibility(View.VISIBLE);
                    Log.d("Movimento executado", "Movimento Executado: esquerda" + listaMovimentos.size());
                    locomover(270.0f, VELOCIDADE_SPHERO);
                    break;
                case R.mipmap.seta_direita:
                    Log.d("Movimento executado", "Movimento Executado: direita" + listaMovimentos.size());
                    setadireita.setVisibility(View.VISIBLE);
                    locomover(90.0f, VELOCIDADE_SPHERO);
                    break;
                case R.mipmap.seta_baixo:
                    Log.d("Movimento executado", "Movimento Executado: baixo" + listaMovimentos.size());
                    setabaixo.setVisibility(View.VISIBLE);
                    locomover(180.0f, VELOCIDADE_SPHERO);
                    break;
                case R.mipmap.seta_cima:
                    Log.d("Movimento executado", "Movimento Executado: cima" + listaMovimentos.size());
                    setacima.setVisibility(View.VISIBLE);
                    locomover(0.0f, VELOCIDADE_SPHERO);
                    break;
            }
            listaMovimentos.remove(0);
            adapter.notifyDataSetChanged();
        } else {
            executandoMovimentos = false;
        }

    }


    private void locomover(final float direcao, final float velocidade) {
        if (mRobot != null) {
            // Sphero ira para a direcao requerida na velocidade que for passada como parametro
            mRobot.drive(direcao, velocidade);

            // ira se locomover na direcao por 1 segundo
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    //Para e executa o proximo movimento da lista recursivamente.
                    mRobot.stop();
                    executarListaMovimentos();
                }
            }, 1000);

        }
    }

    private boolean isUltimoInserido(int imagemId) {
        return !listaMovimentos.isEmpty() && listaMovimentos.get(listaMovimentos.size() - 1) == imagemId;
    }


    //    Esse metodo manipula o sensor de acelerometro do celular
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!executandoMovimentos && listaMovimentos.size() < NUM_MOVIMENTOS) {
            esconderSetas();
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

         /*
        Os valores ocilam de -10 a 10.
        Quanto maior o valor de X mais ele ta caindo para a esquerda - Positivo Esqueda
        Quanto menor o valor de X mais ele ta caindo para a direita  - Negativo Direita
        Se o valor de  X for 0 então o celular ta em pé - Nem Direita Nem Esquerda
        Se o valor de Y for 0 então o cel ta "deitado"
         Se o valor de Y for negativo então ta de cabeça pra baixo, então quanto menor y mais ele ta inclinando pra ir pra baixo
        Se o valor de Z for 0 então o dispositivo esta reto na horizontal.
        Quanto maioro o valor de Z Mais ele esta inclinado para frente
        Quanto menor o valor de Z Mais ele esta inclinado para traz.
        */

            if (z > 1) {
                if (y < -1.5) {
                    setacima.setVisibility(View.VISIBLE);
                    if (!isUltimoInserido(R.mipmap.seta_cima)) {
                        listaMovimentos.add(R.mipmap.seta_cima);
                    }
                } else {
                    if (x == 0) {
                    } else if (x > 2) {
                        setaesquerda.setVisibility(View.VISIBLE);
                        if (!isUltimoInserido(R.mipmap.seta_esquerda)) {
                            listaMovimentos.add(R.mipmap.seta_esquerda);
                        }
                    } else if (x < -2) {
                        setadireita.setVisibility(View.VISIBLE);
                        if (!isUltimoInserido(R.mipmap.seta_direita)) {
                            listaMovimentos.add(R.mipmap.seta_direita);
                        }
                    }
                }
            } else if (z < -1) {
                setabaixo.setVisibility(View.VISIBLE);
                if (!isUltimoInserido(R.mipmap.seta_baixo)) {
                    listaMovimentos.add(R.mipmap.seta_baixo);
                }
            }
            adapter.notifyDataSetChanged();
        } else if (!executandoMovimentos && listaMovimentos.size() == NUM_MOVIMENTOS) {
            esconderSetas();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Retorna a bateria disponivel do Sphero
        bateria = sensor.getPower();
    }
}

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.coretronic.ccpclientlibrary.CCPUtils.CCPStarter;
import com.coretronic.ccpclientlibrary.CCPUtils.Config;
import com.coretronic.ccpclientlibrary.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpservice.ICCPAidlCallback;
import com.coretronic.ccpservice.ICCPAidlInterface;

public class MainActivity extends AppCompatActivity implements CCPAidlInterface {
    private CCPStarter ccpStarter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//### 0.啟動CCP Service所有相關動作
        ccpStarter = new CCPStarter(this, this);
        ccpStarter.start();
    }

//--------------------------------------------------------------------------------------------------
//若需要APP與CCP Service雙向溝通，才需加入以下程式與implements CCPAidlInterface。
//### 1.CCPService 所需參數
    private ICCPAidlInterface iccpAidlInterface = null;
    private ICCPAidlCallback iccpAidlCallback = null;

//### 2.等待您的APP與CPP Service建立溝通管道
    @Override
    public void alreadyConnected() {
        this.iccpAidlInterface = ccpStarter.getIccpAidlInterface();

//### 3.從CCP Service接收資料
        iccpAidlCallback = new ICCPAidlCallback.Stub() {
            @Override
            public void valueChanged(int value) throws RemoteException {
                Log.d("AIDL Resutl", "DataFromService: " + value);
            }
        };

        try {
            iccpAidlInterface.registerCallback(iccpAidlCallback);
        } catch (RemoteException e) { }


//### 4.送資料到CCP Service
        try {
            String sendStringToService = iccpAidlInterface.sendString("test123");
            String sendIntToService = iccpAidlInterface.sendInt(1000);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//### 5.解除CCP Service綁定
        if (Config.isBindService) {
            unbindService(ccpStarter.getServiceConnection());
        }
    }
//--------------------------------------------------------------------------------------------------
}

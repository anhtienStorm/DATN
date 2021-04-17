package bkav.android.btalk.messaging;

import android.content.Context;
import android.util.SparseArray;

import com.android.messaging.BugleApplication;
import com.android.messaging.Factory;
import com.android.messaging.FactoryImpl;
import com.android.messaging.datamodel.DataModelImpl;
import com.android.messaging.datamodel.MemoryCacheManager;
import com.android.messaging.datamodel.ParticipantRefresh;
import com.android.messaging.datamodel.media.BugleMediaCacheManager;
import com.android.messaging.datamodel.media.MediaResourceManager;
import com.android.messaging.sms.BugleCarrierConfigValuesLoader;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BugleGservicesImpl;
import com.android.messaging.util.BugleSubscriptionPrefs;
import com.android.messaging.util.BugleWidgetPrefs;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.MediaUtilImpl;
import com.android.messaging.util.OsUtil;

import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkFactoryImpl extends FactoryImpl {

    private boolean mRegisterSendMessage = false;

    private BtalkFactoryImpl() {
    }

    public static Factory register(final Context applicationContext,
                                   final BugleApplication application) {
        // This only gets called once (from BugleApplication.onCreate), but its not called in tests.
//        Assert.isTrue(!sRegistered);
//        Assert.isNull(Factory.get());
        final BtalkFactoryImpl factory = new BtalkFactoryImpl();
        Factory.setInstance(factory);
        sRegistered = true;
        // At this point Factory is published. Services can now get initialized and depend on
        // Factory.get().
        factory.mApplication = application;
        factory.mApplicationContext = applicationContext;
        factory.mMemoryCacheManager = new MemoryCacheManager();
        factory.mMediaCacheManager = new BugleMediaCacheManager();
        factory.mMediaResourceManager = new MediaResourceManager();
        factory.mBugleGservices = new BugleGservicesImpl(applicationContext);
        factory.mBugleApplicationPrefs = new BugleApplicationPrefs(applicationContext);
        factory.mDataModel = new DataModelImpl(applicationContext);
        factory.mBugleWidgetPrefs = new BugleWidgetPrefs(applicationContext);
        //Bkav QuangNdb custom khoi tao cua mUIItent cho nay
        factory.mUIIntents = new BtalkUIIntentsImpl();

        factory.mContactContentObserver = new ParticipantRefresh.ContactContentObserver();
        factory.mMediaUtil = new MediaUtilImpl();
        factory.mSubscriptionPrefs = new SparseArray<BugleSubscriptionPrefs>();
        factory.mCarrierConfigValuesLoader = new BugleCarrierConfigValuesLoader(applicationContext);
        Assert.initializeGservices(factory.mBugleGservices);
        LogUtil.initializeGservices(factory.mBugleGservices);

        if (OsUtil.hasRequiredPermissions()) {
            factory.onRequiredPermissionsAcquired();
        }
        return factory;
    }

    // Anhdts dang ki la gui tin nhan trong app
    public boolean getRegisterSendMessage() {
        if (mRegisterSendMessage) {
            mRegisterSendMessage = false;
            return true;
        }
        return false;
    }

    public void registerSendMessage() {
        mRegisterSendMessage = true;
    }
}

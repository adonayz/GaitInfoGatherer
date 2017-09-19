package edu.wpi.gaitinfogatherer;

import android.app.Application;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.database.sqlite.DatabaseHelper;
import org.researchstack.backbone.storage.file.EncryptionProvider;
import org.researchstack.backbone.storage.file.FileAccess;
import org.researchstack.backbone.storage.file.PinCodeConfig;
import org.researchstack.backbone.storage.file.SimpleFileAccess;
import org.researchstack.backbone.storage.file.UnencryptedProvider;

/**
 * Created by Adonay on 9/8/2017.
 */

public class GaitInfoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PinCodeConfig pinCodeConfig = new PinCodeConfig();

        EncryptionProvider encryptionProvider = new UnencryptedProvider();

        FileAccess fileAccess = new SimpleFileAccess();

        AppDatabase database = new DatabaseHelper(this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION);

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database);
    }
}

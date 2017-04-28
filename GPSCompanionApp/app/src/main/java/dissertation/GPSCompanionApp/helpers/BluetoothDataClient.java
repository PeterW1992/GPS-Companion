package dissertation.GPSCompanionApp.helpers;

/**
 * Created by Peter on 19/02/2017.
 */

public interface BluetoothDataClient {

    void returnData(Constants.RequestType requestType, Object data);

    void returnError(String errorMessage);
}

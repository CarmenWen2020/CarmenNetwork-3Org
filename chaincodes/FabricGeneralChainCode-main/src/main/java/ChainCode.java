
import lombok.extern.java.Log;
import org.apache.commons.collections4.IterableUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * author CarmenWen
 * date: 04/17/2022
 */

@Contract(
        name = "FabricGeneralChainCode",
        info = @Info(
                title = "FabricGeneralChainCode contract",
                description = "The hyperledger Fabric FabricGeneralChainCode contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "jwen758@aucklanduni.ac.nz",
                        name = "CarmenWen",
                        url = "www.github.com/CarmenWen2020")))
@Default
@Log
public class ChainCode implements ContractInterface {

    @Transaction
    public void initLedger(final Context ctx) {
        String value = "{\n" +
                "    \"name\":\" FabricGeneralChainCode\",\n" +
                "    \"email\":\"jwen758@aucklanduni.ac.nz\",\n" +
                "    \"author\":\"CarmenWen\",\n" +
                "    \"github\":\"www.github.com/CarmenWen2020\"\n" +
                '}';
        ChaincodeStub stub = ctx.getStub();
        stub.putStringState("FabricGeneralChainCode" , value);
    }



    @Transaction
    public static QueryResultList richQuery(final Context ctx, String s) {
        return InvokingMethod.richQuery(ctx,s);
    }

    @Transaction
    public static QueryResultList queryAllByKey(final Context ctx, final String key){
        return InvokingMethod.queryAllByKey(ctx,key);
    }

    @Transaction
    public String queryData(final Context ctx, final String key) {
        return InvokingMethod.queryData(ctx,key);
    }

    @Transaction
    public static String createData(final Context ctx, String key, String jsonData) throws ClassNotFoundException {
        return InvokingMethod.createData(ctx,key,jsonData);
    }
    @Transaction
    public static String deleteData(final Context ctx, final String key){
        return InvokingMethod.deleteData(ctx,key);
    }

    @Transaction
    public static String updateData(final Context ctx, final String key,String value){
        return InvokingMethod.updateData(ctx,key,value);
    }





    @Override
    public void beforeTransaction(Context ctx) {
        log.info("*************************************** beforeTransaction ***************************************");
    }


    @Override
    public void afterTransaction(Context ctx, Object result) {
        log.info("*************************************** afterTransaction ***************************************");
        System.out.println("result --------> " + result);
    }










}

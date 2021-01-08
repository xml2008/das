package com.ppdai.das.tx;

import com.ppdai.das.service.DasException;
import com.ppdai.das.service.DasRequest;
import com.ppdai.das.service.DasResult;
import com.ppdai.das.service.DasService;
import com.ppdai.das.service.TxBeginRequest;
import com.ppdai.das.service.TxBeginResponse;
import com.ppdai.das.service.TxCommitCommandRequest;
import com.ppdai.das.service.TxCommitCommandResonse;
import com.ppdai.das.service.TxCommitRequest;
import com.ppdai.das.service.TxCommitResponse;
import com.ppdai.das.service.TxRegisterApplicationRequest;
import com.ppdai.das.service.TxRegisterApplicationResponse;
import com.ppdai.das.service.TxXID;
import com.ppdai.das.tx.event.BusEvent;
import com.ppdai.das.tx.monitor.BusEventManager;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

public class MockClient extends DasService.Client{
    public MockClient(TProtocol prot) {
        super(prot);
    }

    public MockClient(TProtocol iprot, TProtocol oprot) {
        super(iprot, oprot);
    }

  /*  @Override
    public TxRegisterApplicationResponse registerApplication(TxRegisterApplicationRequest req) throws TException {
        BusEventManager.post(new BusEvent(req.toString()));
        TxRegisterApplicationResponse response = new TxRegisterApplicationResponse();
        response.setResult("success");
        return response;
    }

    @Override
    public TxBeginResponse txBegin(TxBeginRequest req) throws TException {
        BusEventManager.post(new BusEvent(req.toString()));
        TxBeginResponse response = new TxBeginResponse();
        TxXID id = new TxXID();
        id.setNumber("1234567");
        id.setIp("172.20.19.106");
        response.setXid(id);
        return response;
    }
*/
 /*   @Override
    public TxCommitResponse txCommit(TxCommitRequest req) throws TException {
        BusEventManager.post(new BusEvent(req.toString()));

        TxCommitResponse response = new TxCommitResponse();
        response.setResult("success");
        return response;
    }*/
/*
    public TxCommitCommandResonse txCommitCommand(TxCommitCommandRequest req) {
        BusEventManager.post(new BusEvent(req.toString()));

    }*/
}

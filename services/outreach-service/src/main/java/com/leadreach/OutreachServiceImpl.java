package com.leadreach;

import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import leadreach.OutreachServiceGrpc;
import leadreach.OutreachServiceOuterClass.SendRequest;
import leadreach.OutreachServiceOuterClass.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OutreachServiceImpl extends OutreachServiceGrpc.OutreachServiceImplBase {
    
    private static final Logger LOG = LoggerFactory.getLogger(OutreachServiceImpl.class);
    
    @Override
    public void send(SendRequest request, StreamObserver<SendResponse> responseObserver) {
        // Log that email was sent
        LOG.info("Email sent to: {}", request.getLead().getEmail());
        
        // Create and send the response with success=true
        SendResponse response = SendResponse.newBuilder()
            .setSuccess(true)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

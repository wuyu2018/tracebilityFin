package com.foodtraceability.agent.consensus.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: pbft_consensus.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ConsensusServiceGrpc {

  private ConsensusServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "ConsensusService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto,
      com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> getInitiateConsensusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "InitiateConsensus",
      requestType = com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto,
      com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> getInitiateConsensusMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto, com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> getInitiateConsensusMethod;
    if ((getInitiateConsensusMethod = ConsensusServiceGrpc.getInitiateConsensusMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getInitiateConsensusMethod = ConsensusServiceGrpc.getInitiateConsensusMethod) == null) {
          ConsensusServiceGrpc.getInitiateConsensusMethod = getInitiateConsensusMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto, com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "InitiateConsensus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("InitiateConsensus"))
              .build();
        }
      }
    }
    return getInitiateConsensusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto,
      com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> getEndorseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Endorse",
      requestType = com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto,
      com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> getEndorseMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto, com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> getEndorseMethod;
    if ((getEndorseMethod = ConsensusServiceGrpc.getEndorseMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getEndorseMethod = ConsensusServiceGrpc.getEndorseMethod) == null) {
          ConsensusServiceGrpc.getEndorseMethod = getEndorseMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto, com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Endorse"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("Endorse"))
              .build();
        }
      }
    }
    return getEndorseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrePrepareMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendPrePrepare",
      requestType = com.foodtraceability.agent.consensus.grpc.PbftMessageProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.AckResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrePrepareMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrePrepareMethod;
    if ((getSendPrePrepareMethod = ConsensusServiceGrpc.getSendPrePrepareMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getSendPrePrepareMethod = ConsensusServiceGrpc.getSendPrePrepareMethod) == null) {
          ConsensusServiceGrpc.getSendPrePrepareMethod = getSendPrePrepareMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendPrePrepare"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.PbftMessageProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.AckResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("SendPrePrepare"))
              .build();
        }
      }
    }
    return getSendPrePrepareMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrepareMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendPrepare",
      requestType = com.foodtraceability.agent.consensus.grpc.PbftMessageProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.AckResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrepareMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendPrepareMethod;
    if ((getSendPrepareMethod = ConsensusServiceGrpc.getSendPrepareMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getSendPrepareMethod = ConsensusServiceGrpc.getSendPrepareMethod) == null) {
          ConsensusServiceGrpc.getSendPrepareMethod = getSendPrepareMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendPrepare"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.PbftMessageProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.AckResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("SendPrepare"))
              .build();
        }
      }
    }
    return getSendPrepareMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendCommitMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendCommit",
      requestType = com.foodtraceability.agent.consensus.grpc.PbftMessageProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.AckResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendCommitMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto> getSendCommitMethod;
    if ((getSendCommitMethod = ConsensusServiceGrpc.getSendCommitMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getSendCommitMethod = ConsensusServiceGrpc.getSendCommitMethod) == null) {
          ConsensusServiceGrpc.getSendCommitMethod = getSendCommitMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.PbftMessageProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendCommit"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.PbftMessageProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.AckResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("SendCommit"))
              .build();
        }
      }
    }
    return getSendCommitMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.BlockNotificationProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getNotifyBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "NotifyBlock",
      requestType = com.foodtraceability.agent.consensus.grpc.BlockNotificationProto.class,
      responseType = com.foodtraceability.agent.consensus.grpc.AckResponseProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.BlockNotificationProto,
      com.foodtraceability.agent.consensus.grpc.AckResponseProto> getNotifyBlockMethod() {
    io.grpc.MethodDescriptor<com.foodtraceability.agent.consensus.grpc.BlockNotificationProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto> getNotifyBlockMethod;
    if ((getNotifyBlockMethod = ConsensusServiceGrpc.getNotifyBlockMethod) == null) {
      synchronized (ConsensusServiceGrpc.class) {
        if ((getNotifyBlockMethod = ConsensusServiceGrpc.getNotifyBlockMethod) == null) {
          ConsensusServiceGrpc.getNotifyBlockMethod = getNotifyBlockMethod =
              io.grpc.MethodDescriptor.<com.foodtraceability.agent.consensus.grpc.BlockNotificationProto, com.foodtraceability.agent.consensus.grpc.AckResponseProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "NotifyBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.BlockNotificationProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.foodtraceability.agent.consensus.grpc.AckResponseProto.getDefaultInstance()))
              .setSchemaDescriptor(new ConsensusServiceMethodDescriptorSupplier("NotifyBlock"))
              .build();
        }
      }
    }
    return getNotifyBlockMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ConsensusServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceStub>() {
        @java.lang.Override
        public ConsensusServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConsensusServiceStub(channel, callOptions);
        }
      };
    return ConsensusServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ConsensusServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceBlockingStub>() {
        @java.lang.Override
        public ConsensusServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConsensusServiceBlockingStub(channel, callOptions);
        }
      };
    return ConsensusServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ConsensusServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConsensusServiceFutureStub>() {
        @java.lang.Override
        public ConsensusServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConsensusServiceFutureStub(channel, callOptions);
        }
      };
    return ConsensusServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void initiateConsensus(com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInitiateConsensusMethod(), responseObserver);
    }

    /**
     */
    default void endorse(com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getEndorseMethod(), responseObserver);
    }

    /**
     */
    default void sendPrePrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendPrePrepareMethod(), responseObserver);
    }

    /**
     */
    default void sendPrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendPrepareMethod(), responseObserver);
    }

    /**
     */
    default void sendCommit(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendCommitMethod(), responseObserver);
    }

    /**
     */
    default void notifyBlock(com.foodtraceability.agent.consensus.grpc.BlockNotificationProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getNotifyBlockMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ConsensusService.
   */
  public static abstract class ConsensusServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ConsensusServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ConsensusService.
   */
  public static final class ConsensusServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ConsensusServiceStub> {
    private ConsensusServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConsensusServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConsensusServiceStub(channel, callOptions);
    }

    /**
     */
    public void initiateConsensus(com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInitiateConsensusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void endorse(com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getEndorseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendPrePrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendPrePrepareMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendPrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendPrepareMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendCommit(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendCommitMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void notifyBlock(com.foodtraceability.agent.consensus.grpc.BlockNotificationProto request,
        io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getNotifyBlockMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ConsensusService.
   */
  public static final class ConsensusServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ConsensusServiceBlockingStub> {
    private ConsensusServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConsensusServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConsensusServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto initiateConsensus(com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInitiateConsensusMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto endorse(com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getEndorseMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.AckResponseProto sendPrePrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendPrePrepareMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.AckResponseProto sendPrepare(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendPrepareMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.AckResponseProto sendCommit(com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendCommitMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.foodtraceability.agent.consensus.grpc.AckResponseProto notifyBlock(com.foodtraceability.agent.consensus.grpc.BlockNotificationProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getNotifyBlockMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ConsensusService.
   */
  public static final class ConsensusServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ConsensusServiceFutureStub> {
    private ConsensusServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConsensusServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConsensusServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto> initiateConsensus(
        com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInitiateConsensusMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto> endorse(
        com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getEndorseMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.AckResponseProto> sendPrePrepare(
        com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendPrePrepareMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.AckResponseProto> sendPrepare(
        com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendPrepareMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.AckResponseProto> sendCommit(
        com.foodtraceability.agent.consensus.grpc.PbftMessageProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendCommitMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.foodtraceability.agent.consensus.grpc.AckResponseProto> notifyBlock(
        com.foodtraceability.agent.consensus.grpc.BlockNotificationProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getNotifyBlockMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_INITIATE_CONSENSUS = 0;
  private static final int METHODID_ENDORSE = 1;
  private static final int METHODID_SEND_PRE_PREPARE = 2;
  private static final int METHODID_SEND_PREPARE = 3;
  private static final int METHODID_SEND_COMMIT = 4;
  private static final int METHODID_NOTIFY_BLOCK = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INITIATE_CONSENSUS:
          serviceImpl.initiateConsensus((com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto>) responseObserver);
          break;
        case METHODID_ENDORSE:
          serviceImpl.endorse((com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto>) responseObserver);
          break;
        case METHODID_SEND_PRE_PREPARE:
          serviceImpl.sendPrePrepare((com.foodtraceability.agent.consensus.grpc.PbftMessageProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto>) responseObserver);
          break;
        case METHODID_SEND_PREPARE:
          serviceImpl.sendPrepare((com.foodtraceability.agent.consensus.grpc.PbftMessageProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto>) responseObserver);
          break;
        case METHODID_SEND_COMMIT:
          serviceImpl.sendCommit((com.foodtraceability.agent.consensus.grpc.PbftMessageProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto>) responseObserver);
          break;
        case METHODID_NOTIFY_BLOCK:
          serviceImpl.notifyBlock((com.foodtraceability.agent.consensus.grpc.BlockNotificationProto) request,
              (io.grpc.stub.StreamObserver<com.foodtraceability.agent.consensus.grpc.AckResponseProto>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getInitiateConsensusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.ConsensusRequestProto,
              com.foodtraceability.agent.consensus.grpc.ConsensusResponseProto>(
                service, METHODID_INITIATE_CONSENSUS)))
        .addMethod(
          getEndorseMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.EndorsementRequestProto,
              com.foodtraceability.agent.consensus.grpc.EndorsementResponseProto>(
                service, METHODID_ENDORSE)))
        .addMethod(
          getSendPrePrepareMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
              com.foodtraceability.agent.consensus.grpc.AckResponseProto>(
                service, METHODID_SEND_PRE_PREPARE)))
        .addMethod(
          getSendPrepareMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
              com.foodtraceability.agent.consensus.grpc.AckResponseProto>(
                service, METHODID_SEND_PREPARE)))
        .addMethod(
          getSendCommitMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.PbftMessageProto,
              com.foodtraceability.agent.consensus.grpc.AckResponseProto>(
                service, METHODID_SEND_COMMIT)))
        .addMethod(
          getNotifyBlockMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.foodtraceability.agent.consensus.grpc.BlockNotificationProto,
              com.foodtraceability.agent.consensus.grpc.AckResponseProto>(
                service, METHODID_NOTIFY_BLOCK)))
        .build();
  }

  private static abstract class ConsensusServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ConsensusServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.foodtraceability.agent.consensus.grpc.PbftConsensus.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ConsensusService");
    }
  }

  private static final class ConsensusServiceFileDescriptorSupplier
      extends ConsensusServiceBaseDescriptorSupplier {
    ConsensusServiceFileDescriptorSupplier() {}
  }

  private static final class ConsensusServiceMethodDescriptorSupplier
      extends ConsensusServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ConsensusServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ConsensusServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ConsensusServiceFileDescriptorSupplier())
              .addMethod(getInitiateConsensusMethod())
              .addMethod(getEndorseMethod())
              .addMethod(getSendPrePrepareMethod())
              .addMethod(getSendPrepareMethod())
              .addMethod(getSendCommitMethod())
              .addMethod(getNotifyBlockMethod())
              .build();
        }
      }
    }
    return result;
  }
}

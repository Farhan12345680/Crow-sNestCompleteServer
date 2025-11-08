package org.example;

import com.ClientSerializedClasses.currentlyBusy;
import com.ClientSerializedClasses.currentlyInactive;
import com.PostFile.*;
import com.comment.commentData;
import com.comment.commentDataDelete;
import com.comment.commentDataRequest;
import com.comment.commentEdit;
import com.crowsnestfrontend.SerializedClasses.*;
import com.groupManagement.*;
import com.groupManagement.groupMessaging.*;
import org.example.DatabaseCreation.DatabaseCreation;
import org.example.DatabaseCreation.InmemoryDataBaseCreation;
import org.example.DatabaseCreation.getAllUserDataFrom;
import org.example.GroupMessage.GroupHandler;
import org.example.GroupMessage.GroupMessageHandler;
import org.example.Messages.MessageData;
import org.example.PostProvider.commentProvider;
import org.example.PostProvider.postProviderHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.DatabaseCreation.getAllUserDataFrom.updateFriendStatus;

public class server2 {
    public static int PORT = 12346;
    public static ConcurrentHashMap<String, BlockingQueue<payload>> payloadBlockingQueue = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket sc = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket client = sc.accept();
                Thread.startVirtualThread(() -> handlePayloadClient(client));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void handlePayloadClient(Socket client) {
        ObjectOutputStream writer = null;
        ObjectInputStream reader = null;
        String name1 =null;
        try {
            writer = new ObjectOutputStream(client.getOutputStream());
            reader = new ObjectInputStream(client.getInputStream());

            SignInProfile nameObject = (SignInProfile) reader.readObject();
            String name = nameObject.getName();
            name1=name;

            ObjectOutputStream finalWriter1 = writer;
            if(nameObject instanceof signInProfileWithTimeStamp ){
                Thread.startVirtualThread(() -> {
                    getAllUserDataFrom.getUsers(name,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone));
                    getAllUserDataFrom.getRequestingSituation(name,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone));
                    getAllUserDataFrom.getRequestingSituationOwner(name,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone));
                    getAllUserDataFrom.showAllFriends(name,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone));
                    //MessageData.getAllMessage(name ,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone),finalWriter1);
                    getAllUserDataFrom.makeBlockUser(name  ,(((signInProfileWithTimeStamp) nameObject).UTCtimeZone) , finalWriter1);

                });
            }
            else{
                String dataImage = DatabaseCreation.getUserImageData(name);
                payloadBlockingQueue.putIfAbsent(name, new LinkedBlockingQueue<>());


                Thread.startVirtualThread(() -> {
                    for (var key : payloadBlockingQueue.keySet()) {
                        if (key.equals(name)) continue;
                        payloadBlockingQueue.get(key)
                                .add(new payLoadUsers(key, new RequestUsers(name, dataImage)));
                    }

                    getAllUserDataFrom.getUsers(name,"-4713-11-24 00:00:00");
                    getAllUserDataFrom.getRequestingSituation(name ,"-4713-11-24 00:00:00");
                    getAllUserDataFrom.getRequestingSituationOwner(name ,"-4713-11-24 00:00:00");
                    getAllUserDataFrom.showAllFriends(name ,"-4713-11-24 00:00:00");

                });
            }


            ObjectOutputStream finalWriter = writer;


            Thread.startVirtualThread(() -> {
                try {
                    while (true) {
                        payload obj = payloadBlockingQueue.get(name).take();

                        switch (obj) {
                            case showAllBlockedUsers show ->{
                                DatabaseCreation.showAllBlockUsers1(show , finalWriter);
                            }
                            case makeUnblock un ->{
                                DatabaseCreation.makeUnBlock1(un , finalWriter);
                            }
                            case deleteMember del->{
                                GroupHandler.deleteInviteGroup(del);
                            }
                            case EditGroupMessage edit ->{
                                GroupMessageHandler.EditGroupMessageHandler(edit);
                            }
                            case DeleteGroupMessage del ->{
                                GroupMessageHandler.DeleteMessage(del);
                            }
                            case groupMessagingRange range->{
                                GroupMessageHandler.getMessages(range, finalWriter);
                            }
                            case groupMessagingSender sender->{
                                GroupMessageHandler.insertIntoDatabase(sender);
                            }
                            case acceptInviteGroup give->{
                                GroupHandler.makeInviteAccept(give);
                            }
                            case deleteGroupInvite del->{
                                GroupHandler.deleteInviteGroup(del);
                            }
                            case giveGroupInvite give ->{
                                GroupHandler.makeInvite(give);
                            }
                            case getGroupMemberNames get->{
                                GroupHandler.getMemberNamesofAChannel(get,finalWriter);
                            }
                            case showGroupInviteRange range->{
                                GroupHandler.giveGroupDataRangeInvite(range , finalWriter);
                            }
                            case GiveMeDataRange range->{
                                System.out.println("Group data request came ");
                                GroupHandler.giveGroupDataRange(range, finalWriter);
                            }
                            case createGroup groupReq ->{
                                GroupHandler.groupCreation(groupReq ,finalWriter);
                            }
                            case DeleteGroup grp -> {
                                GroupHandler.deleteGroup(grp);
                            }

                            case commentDataDelete del->{
                                commentProvider.deleteComment(del.commentID);
                            }
                            case commentEdit edit ->{
                                System.out.println("Comment edit request came");
                                commentProvider.editComment(edit.commentID, edit);
                            }
                            case commentDataRequest req->{
                                System.out.println("Comment request came !!! ");
                                commentProvider.getPost(req ,finalWriter);
                            }
                            case commentData comment ->{
                                System.out.println("comment came");
                                commentProvider.HandlePost(comment);
                            }

                            case getMyPost my ->{
                                postProviderHandler.getMyPost(my ,finalWriter);
                            }
                            case DeleteFile del->{
                                Thread.startVirtualThread(()->{
                                    postProviderHandler.deletePost(del.id);
                                });
                            }
                            case EditFile edit->{
                                Thread.startVirtualThread(()->{
                                    postProviderHandler.editPost(edit.id,edit);
                                });
                            }
                            case PostData data ->{
                                System.out.println("post data content came ");
                                postProviderHandler.HandlePost(data);
                            }
                            case getPostRequest get ->{

                                System.out.println("postData request came");
                                postProviderHandler.getPost(get ,finalWriter);
                            }

                            case iceObject ice->{
                                if(name.equals(ice.candidate)){
                                    synchronized (finalWriter){
                                        finalWriter.writeObject(ice);
                                        finalWriter.flush();
                                    }
                                }
                                else if (payloadBlockingQueue.containsKey(ice.candidate)) {
                                    payloadBlockingQueue.get(ice.candidate).add(ice);
                                }

                            }

                            case returnIceObject ice->{
                                if(name.equals(ice.candidate)){
                                    synchronized (finalWriter){
                                        finalWriter.writeObject(ice);
                                        finalWriter.flush();
                                    }
                                }
                                else if (payloadBlockingQueue.containsKey(ice.candidate)) {
                                    payloadBlockingQueue.get(ice.candidate).add(ice);
                                }

                            }

                            case webrtcConnection sdp -> {
                                String type = sdp.type;

                                if ("OFFER".equals(type)) {
                                    System.out.println("An offer received");
                                    System.out.println("sender --> " + sdp.userId);
                                    System.out.println("receiver --> " + sdp.candidate);
                                    System.out.println("receiver active? " + InmemoryDataBaseCreation.isUserAlreadyActive(sdp.candidate));
                                    System.out.println("receiver busy? " + InmemoryDataBaseCreation.isUserBusy(sdp.candidate));

                                    if(DatabaseCreation.blockedUserChecker(sdp.clientName ,sdp.candidate)){
                                        System.out.println("this other user is blocked");
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(new currentlyInactive());
                                            finalWriter.flush();
                                        }
                                        break;
                                    }
                                    if (!InmemoryDataBaseCreation.isUserAlreadyActive(sdp.candidate)) {
                                        System.out.println("receiver inactive – notifying caller");
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(new currentlyInactive());
                                            finalWriter.flush();
                                        }
                                    }
                                    else if (InmemoryDataBaseCreation.isUserBusy(sdp.candidate)) {
                                        System.out.println("receiver busy – notifying caller");
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(new currentlyBusy());
                                            finalWriter.flush();
                                        }
                                    }
                                    else if (name.equals(sdp.candidate)) {
                                        InmemoryDataBaseCreation.insertBusyUser(sdp.userId);
                                        InmemoryDataBaseCreation.insertBusyUser(sdp.candidate);
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(sdp);
                                            finalWriter.flush();
                                        }
                                    }
                                    else if (payloadBlockingQueue.containsKey(sdp.candidate)) {
                                        payloadBlockingQueue.get(sdp.candidate).add(sdp);
                                    }

                                }
                                else if ("ANSWER".equals(type)) {
                                    System.out.println("An answer came");
                                    System.out.println("sender --> " + sdp.userId);
                                    System.out.println("receiver --> " + sdp.candidate);

                                    if (name.equals(sdp.candidate)) {
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(sdp);
                                            finalWriter.flush();
                                        }
                                    } else if (payloadBlockingQueue.containsKey(sdp.candidate)) {
                                        payloadBlockingQueue.get(sdp.candidate).add(sdp);
                                    }

                                } else if ("ICE".equals(type)) {
                                    System.out.println("ICE candidate came");

                                    if (name.equals(sdp.candidate)) {
                                        synchronized (finalWriter) {
                                            finalWriter.writeObject(sdp);
                                            finalWriter.flush();
                                        }
                                    } else if (payloadBlockingQueue.containsKey(sdp.candidate)) {
                                        payloadBlockingQueue.get(sdp.candidate).add(sdp);
                                    }
                                }
                            }

                            case deleteMessage del ->{
                                MessageData.deleteMessages(del);

                                if(del.initiator.equals(name)){
                                    System.out.println("this was called ");
                                    if (payloadBlockingQueue.containsKey(del.receiver)) {
                                        payloadBlockingQueue.get(del.receiver).add(del);
                                    }
                                }else{
                                   synchronized (finalWriter){
                                       finalWriter.writeObject(del);
                                       finalWriter.flush();
                                   }
                                }

                                System.out.println(del.receiver +"<-->"+del.initiator+"<--->");
                            }

                            case Unfriend unfriend -> {
                                DatabaseCreation.makeUNFriend(unfriend);
                                String tempString = unfriend.unfriendReceiver;

                                UtilityUpdateStatusSender(tempString, unfriend.clientName, 13);
                            }

                            case MessageGetterRequest msg -> Thread.startVirtualThread(() ->{
                                    MessageData.getAllMessage(obj.clientName , msg.time ,finalWriter);
                                    getAllUserDataFrom.allMessageReaction(obj.clientName  ,msg.time , finalWriter1);
                            });

                            case MakeFriendRequest makeFriendRequest -> {
                                String clientName = obj.clientName;
                                String targetName = makeFriendRequest.getName();
                                DatabaseCreation.makeRequests(clientName, targetName);
                                UtilityUpdateStatusSender(targetName ,clientName ,1);
                            }

                            case MakeBlock makeBlock ->{
                                String receiver = makeBlock.nameGetter();
                                DatabaseCreation.makeBlock(obj.clientName,receiver );
                                System.out.println(receiver+"<---- name came here");
                                try {
                                    if (payloadBlockingQueue.containsKey(receiver)) {
                                        System.out.println("name came here 2");
                                        payloadBlockingQueue.get(receiver).add(
                                                new updateStatus(receiver, obj.clientName, -144)
                                        );
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            case MakeFriendRequestAccept makeFriendRequestAccept -> {
                                String targetName = makeFriendRequestAccept.getName();
                                String clientName = obj.clientName;
                                updateFriendStatus(clientName, targetName);
                                UtilityUpdateStatusSender(clientName ,targetName ,3);
                                UtilityUpdateStatusSender(targetName ,clientName ,3);
                            }

                            case deleteFriendRequest deleteFriendRequest -> {
                                String clientName = obj.clientName;
                                String targetName = deleteFriendRequest.getName();
                                DatabaseCreation.deleteRequests(clientName, targetName);

                                UtilityUpdateStatusSender(targetName ,clientName ,-1);
                            }

                            case removeIncomingFriendRequest removeIncomingFriendRequest -> {
                                String targetName = obj.clientName;
                                String clientName = removeIncomingFriendRequest.getString();
                                DatabaseCreation.deleteRequests(clientName, targetName);

                                UtilityUpdateStatusSender(clientName ,targetName ,0);
                            }

                            case Message message -> {
                                String nameOfReceiver = message.name();
                                String text1 = message.getText();
                                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                                String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                System.out.println("this is the messageURl "+message.getMessageURL());
                                System.out.println("this ");
                                int id=0;
                                if (payloadBlockingQueue.containsKey(nameOfReceiver)) {
                                    id=MessageData.makeMessage(obj.clientName, (Message) obj, 1, message.getIsReply() ,message.getMessageType() ,message.getMessageURL());
                                } else {
                                    id=MessageData.makeMessage(obj.clientName, (Message) obj, 0, message.getIsReply(),message.getMessageType(),message.getMessageURL());
                                }
                                System.out.println("this is the id of the message sent --->"+id);
                                MessagePayload payload=new MessagePayload(
                                        message.clientName,
                                        id,
                                        text1,
                                        0,
                                        formattedTime,
                                        message.getMessageType(),
                                        message.getMessageURL()
                                );
                                payload.sender=nameOfReceiver;
                                payload.isReply=message.getIsReply();
                                synchronized (finalWriter) {

                                    finalWriter.writeObject(payload);
                                    finalWriter.flush();
                                }
                                if (payloadBlockingQueue.containsKey(nameOfReceiver)) {
                                      payloadBlockingQueue.get(nameOfReceiver).add(payload);
                                }


                            }

                            case payloadMessageReaction msgReaction -> {
                                String MESSAGE_OWNER=MessageData.returnMessageSender(msgReaction.messageID ,msgReaction);
                                if(MESSAGE_OWNER.equals(name)){
                                    synchronized (finalWriter){
                                        finalWriter.writeObject(msgReaction);
                                        finalWriter.flush();
                                    }
                                    break;
                                }
                                if (payloadBlockingQueue.containsKey(MESSAGE_OWNER)) {
                                         payloadBlockingQueue.get(MESSAGE_OWNER).add(msgReaction);
                                }
                                //MessageData.setReaction(msgReaction.messageID , msgReaction.reactionType);

                            }

                            default -> {

                                synchronized (finalWriter) {
                                    finalWriter.writeObject(obj);
                                    finalWriter.flush();
                                }
                            }
                        }
                    }
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    try {
                        System.out.println("this is the name of the user "+name);
                        InmemoryDataBaseCreation.removerActiveUser(name);
                        InmemoryDataBaseCreation.removeBusyUser(name);
                        client.close();
                    }
                    catch (IOException ignored) {}
                }
            });

            ObjectInputStream finalReader = reader;
            Thread.startVirtualThread(() -> {
                try {
                    while (true) {
                        Object obj = finalReader.readObject();
                        if (obj instanceof payload) {
                            payloadBlockingQueue.get(((payload) obj).clientName).add((payload) obj);
                        } else {
                            System.out.println("This object is unrecognizable");
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });

        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Client handler error: " + e.getMessage());
            e.printStackTrace();
            try {
                System.out.println("this is the name of the user "+name1);
                InmemoryDataBaseCreation.removerActiveUser(name1);
                InmemoryDataBaseCreation.removeBusyUser(name1);
                client.close();
            } catch (IOException ignored) {}
        }
    }

    private static void UtilityUpdateStatusSender(String  client, String reciever, int index) {
        try {
            if (payloadBlockingQueue.containsKey(client)) {
                payloadBlockingQueue.get(client).add(
                        new updateStatus(client, reciever, index)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

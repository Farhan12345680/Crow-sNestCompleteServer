
        package org.example.GroupMessage;

        import com.groupManagement.*;
        import com.groupManagement.groupMessaging.deleteMember;
        import org.example.DatabaseCreation.DatabaseCreation;

        import java.io.ObjectOutputStream;
        import java.sql.*;

public class GroupHandler {

    // Create a new group
    public static void groupCreation(createGroup groupReq, ObjectOutputStream out) {
        String checkQuery = "SELECT COUNT(*) FROM GroupChannel WHERE ChannelName=?";
        String insertGroup = "INSERT INTO GroupChannel(ChannelName, groupDescription, groupImageURL, groupCreator) VALUES(?,?,?,?)";
        String addMember = "INSERT INTO GroupMembers(channelID, memberName, roleID) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL)) {
            try (PreparedStatement ps = conn.prepareStatement(checkQuery)) {
                ps.setString(1, groupReq.groupName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Group name already exists: " + groupReq.groupName);
                        out.writeObject(new groupNameAlreadyExists());
                        out.flush();
                        return;
                    }
                }
            }

            System.out.println(groupReq.groupImageURL+" "+groupReq.groupName +" "+groupReq.clientName +" "+groupReq.groupDescription);
            int channelID;
            try (PreparedStatement ps = conn.prepareStatement(insertGroup, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, groupReq.groupName);
                ps.setString(2, groupReq.groupDescription);
                ps.setString(3, groupReq.groupImageURL != null
                        ? groupReq.groupImageURL
                        : "https://res.cloudinary.com/dvpwqtobj/image/upload/v1758977719/crowd_gfvbey.png");
                ps.setString(4, groupReq.clientName);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        channelID = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated channelID.");
                    }
                }
            }


            try (PreparedStatement ps = conn.prepareStatement(addMember)) {
                ps.setInt(1, channelID);
                ps.setString(2, groupReq.clientName);
                ps.setInt(3, 4);
                ps.executeUpdate();
            }

            synchronized (out) {
                out.writeObject(new groupDataCorrect());
                out.flush();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteGroup(DeleteGroup grp) {
        String query = "DELETE FROM GroupChannel WHERE channelID=?";
        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, grp.ChannelID);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void giveGroupDataRange(GiveMeDataRange range, ObjectOutputStream out) {
        String baseQuery =
                """
                SELECT gc.channelID, gc.ChannelName, gc.groupImageURL, gc.groupDescription,
                       strftime('%Y-%m-%d %H:%M:%S', gc.groupCreationDate) AS formattedCreationDate,
                       gc.groupCreator, gm.roleID, gm.memberID
                FROM GroupChannel gc
                JOIN GroupMembers gm ON gc.channelID = gm.channelID
                WHERE (gm.memberName = ? AND gm.roleID>0)
                """;

        String query;
        if (range.start == -1 && range.end == -1) {
            query = baseQuery + " ORDER BY gc.channelID DESC LIMIT 15";
        } else if (range.start == -1) {
            query = baseQuery + " AND gc.channelID > ? ORDER BY gc.channelID DESC LIMIT 15";
        } else {
            query = baseQuery + " AND gc.channelID < ? ORDER BY gc.channelID DESC LIMIT 15";
        }

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, range.clientName);

            if (range.start == -1 && range.end != -1) {
                ps.setInt(2, range.end);
            } else if (range.start != -1) {
                ps.setInt(2, range.start);
            }

            try (ResultSet rs = ps.executeQuery()) {
                synchronized (out) {
                    while (rs.next()) {
                        System.out.println(rs.getString("ChannelName"));
                        out.writeObject(new groupData(
                                null,
                                rs.getString("ChannelName"),
                                rs.getString("groupDescription"),
                                rs.getString("groupImageURL"),
                                rs.getString("groupCreator"),
                                rs.getInt("channelID"),
                                rs.getInt("roleID"),
                                rs.getInt("memberID"),
                                rs.getString("formattedCreationDate")

                        ));
                        out.flush();
                    }

                    out.writeObject(new groupDataSendingEnder());
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void giveGroupDataRangeInvite(showGroupInviteRange range, ObjectOutputStream out) {
        String baseQuery = """
                SELECT gc.channelID, gc.ChannelName, gc.groupImageURL, gc.groupDescription,
                       strftime('%Y-%m-%d %H:%M:%S', gc.groupCreationDate) AS formattedCreationDate,
                       gc.groupCreator, gm.roleID, gm.memberID
                FROM GroupChannel gc
                JOIN GroupMembers gm ON gc.channelID = gm.channelID
                WHERE (gm.memberName = ? AND gm.roleID = 0)
                """;

        String query;
        if (range.start == -1 && range.end == -1) {
            query = baseQuery + " ORDER BY gc.channelID DESC LIMIT 15";
        } else if (range.start == -1) {
            query = baseQuery + " AND gc.channelID > ? ORDER BY gc.channelID DESC LIMIT 15";
        } else {
            query = baseQuery + " AND gc.channelID < ? ORDER BY gc.channelID DESC LIMIT 15";
        }

        try (Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, range.clientName);

            if (range.start == -1 && range.end != -1) {
                ps.setInt(2, range.end);
            } else if (range.start != -1) {
                ps.setInt(2, range.start);
            }

            try (ResultSet rs = ps.executeQuery()) {
                synchronized (out) {
                    while (rs.next()) {
                        System.out.println(rs.getString("ChannelName"));
                        out.writeObject(new showInviteGroupData(
                                null,
                                rs.getString("ChannelName"),
                                rs.getString("groupDescription"),
                                rs.getString("groupImageURL"),
                                rs.getString("groupCreator"),
                                rs.getInt("channelID"),
                                rs.getInt("roleID"),
                                rs.getInt("memberID"),
                                rs.getString("formattedCreationDate")

                        ));
                        out.flush();
                    }

                    out.writeObject(new showGroupInviteRangeEnd());
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getMemberNamesofAChannel(getGroupMemberNames get ,final ObjectOutputStream out){
        String queryString = """
                Select gm.memberID , gm.memberName , gm.roleID ,prof.image from GroupMembers as gm JOIN  Profiles as prof ON gm.memberName=prof.name_id where channelID =?;
                """;

        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps = conn.prepareStatement(queryString)){
            ps.setInt(1 , get.channelID);
            groupMemberNames obj =new groupMemberNames();


            ResultSet rs =ps.executeQuery();

            while(rs.next()){
                var t=new groupMemberInfo(rs.getString(2), rs.getString(4) ,rs.getInt(1) ,rs.getInt(3));
                obj.dataArraylist.add(t);
            }

            synchronized (out){
                out.writeObject(obj);
                out.flush();
            }
        }catch (Exception e ){
            e.printStackTrace();
        }
    }

    public static void makeInvite(giveGroupInvite give){
        String query = """
                INSERT INTO GroupMembers(channelID, memberName, roleID) VALUES(?,?,?)
                """;

        try(Connection conn =DriverManager.getConnection(DatabaseCreation.URL);
        PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, give.channelID);
            ps.setString(2, give.name);
            ps.setInt(3,0);
            ps.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void makeInviteAccept(acceptInviteGroup grp){
        String query= """
                Update GroupMembers set roleID=1 WHERE (channelID=? AND memberName=?);
                """;
        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps =conn.prepareStatement(query)){

            ps.setInt(1 , grp.channelID);
            ps.setString(2 , grp.clientName);

            ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteInviteGroup(deleteGroupInvite del){
        String query= """
                Delete from GroupMembers where (channelID=? AND memberName=?);
                """;
        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps =conn.prepareStatement(query)){

            ps.setInt(1 , del.channelID);
            ps.setString(2 , del.clientName);

            ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteInviteGroup(deleteMember del){
        String query= """
                Delete from GroupMembers where (channelID=? AND memberName=?);
                """;
        try(Connection conn = DriverManager.getConnection(DatabaseCreation.URL);
            PreparedStatement ps =conn.prepareStatement(query)){

            ps.setInt(1 , del.channelID);
            ps.setString(2 , del.name);

            ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }



}

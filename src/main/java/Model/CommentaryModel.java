package Model;

public class CommentaryModel {
    private Integer commentary_id;
    private Integer fixture_id;
//    private Integer over_count;
//    private Integer ball;
    private String run_type;
    private String commentaryText;
    private Integer batter_id;
    private Integer bowler_id;
    private String date_time;
    private Integer catcher_id;


    public int getCommentaryId() {
        return commentary_id;
    }

    public void setCommentaryId(int commentaryId) {
        this.commentary_id = commentaryId;
    }

    public int getFixtureId() {
        return fixture_id;
    }

    public void setFixtureId(int fixtureId) {
        this.fixture_id = fixtureId;
    }

//    public int getOverCount() {
//        return over_count;
//    }
//
//    public void setOverCount(int overCount) {
//        this.over_count = overCount;
//    }
//
//    public int getBall() {
//        return ball;
//    }
//
//    public void setBall(int ball) {
//        this.ball = ball;
//    }

    public String getRunType() {
        return run_type;
    }

    public void setRunType(String runType) {
        this.run_type = runType;
    }

    public String getCommentaryText() {
        return commentaryText;
    }

    public void setCommentaryText(String commentaryText) {
        this.commentaryText = commentaryText;
    }

    public int getBatterId() {
        return batter_id;
    }

    public void setBatterId(int batterId) {
        this.batter_id = batterId;
    }

    public int getBowlerId() {
        return bowler_id;
    }

    public void setBowlerId(int bowlerId) {
        this.bowler_id = bowlerId;
    }

    public String getDateTime() {
        return date_time;
    }

    public void setDateTime(String dateTime) {
        this.date_time = dateTime;
    }

    public int getCatcherId() {
        return catcher_id;
    }

    public void setCatcherId(int catcherId) {
        this.catcher_id = catcherId;
    }

    public boolean isValid() {
        return fixture_id > 0 ;
    }
}


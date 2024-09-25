package Model;

public class CommentaryVO {
	
    private Integer commentaryId;
    private Integer fixtureId;
    private String runType;
    private String commentaryText;
    private Integer batterId;
    private Integer bowlerId;
    private String dateTime;
    private Integer catcherId;


    public int getCommentaryId() {
        return commentaryId;
    }

    public void setCommentaryId(int commentaryId) {
        this.commentaryId = commentaryId;
    }

    public int getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(int fixtureId) {
        this.fixtureId = fixtureId;
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
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getCommentaryText() {
        return commentaryText;
    }

    public void setCommentaryText(String commentaryText) {
        this.commentaryText = commentaryText;
    }

    public int getBatterId() {
        return batterId;
    }

    public void setBatterId(int batterId) {
        this.batterId = batterId;
    }

    public int getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(int bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getCatcherId() {
        return catcherId;
    }

    public void setCatcherId(int catcherId) {
        this.catcherId = catcherId;
    }

    public boolean isValid() {
        return fixtureId > 0 ;
    }
}


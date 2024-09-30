package model;

public class MatchDetailVO {
	
	private Integer fixture_id;
	private Integer toss_win;
	private Integer man_of_the_match;
	private String toss_win_decision;
	
	public Integer getFixture_id() {
        return fixture_id;
    }

    public void setFixture_id(Integer fixture_id) {
        this.fixture_id = fixture_id;
    }

    public Integer getToss_win() {
        return toss_win;
    }

    public void setToss_win(Integer toss_win) {
        this.toss_win = toss_win;
    }

    public Integer getMan_of_the_match() {
        return man_of_the_match;
    }

    public void setMan_of_the_match(Integer man_of_the_match) {
        this.man_of_the_match = man_of_the_match;
    }

    public String getToss_win_decision() {
        return toss_win_decision;
    }

    public void setToss_win_decision(String toss_win_decision) {
        this.toss_win_decision = toss_win_decision;
    }
	
}

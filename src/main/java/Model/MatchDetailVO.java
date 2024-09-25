package model;

public class MatchDetailVO {
	
	private int fixture_id = -1;
	private int toss_win = -1;
	private int man_of_the_match = -1;
	private String toss_win_decision;
	
	public int getFixture_id() {
        return fixture_id;
    }

    public void setFixture_id(int fixture_id) {
        this.fixture_id = fixture_id;
    }

    public int getToss_win() {
        return toss_win;
    }

    public void setToss_win(int toss_win) {
        this.toss_win = toss_win;
    }

    public int getMan_of_the_match() {
        return man_of_the_match;
    }

    public void setMan_of_the_match(int man_of_the_match) {
        this.man_of_the_match = man_of_the_match;
    }

    public String getToss_win_decision() {
        return toss_win_decision;
    }

    public void setToss_win_decision(String toss_win_decision) {
        this.toss_win_decision = toss_win_decision;
    }
	
}

package Model;

public class TourTeam {
    private int team_id = -1;
    private double net_run_rate = 0.0;
    private int points = 0;

    public int getTeamId() {
        return team_id;
    }

    public void setTeamId(int team_id) {
        this.team_id = team_id;
    }

    public double getNetRunRate() {
        return net_run_rate;
    }

    public void setNetRunRate(double net_run_rate) {
        this.net_run_rate = net_run_rate;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}


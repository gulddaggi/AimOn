package com.example.ffp_be.like.service;

import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.user.entity.User;

import java.util.List;

public interface TeamLikeService {

    void likeTeam(User user, Team team);

    void unlikeTeam(User user, Team team);

    List<Team> getLikedTeams(User user);

    boolean toggleTeamLike(User user, Team team);
}

package com.example.ffp_be.like.service;

import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.like.entity.TeamLike;
import com.example.ffp_be.like.repository.TeamLikeRepository;
import com.example.ffp_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamLikeServiceImpl implements TeamLikeService {

    private final TeamLikeRepository teamLikeRepository;

    @Override
    @Transactional
    public void likeTeam(User user, Team team) {
        if (teamLikeRepository.findByUserAndTeam(user, team).isEmpty()) {
            teamLikeRepository.save(TeamLike.builder()
                .user(user)
                .team(team)
                .createdAt(LocalDateTime.now())
                .build());
        }
    }

    @Override
    @Transactional
    public void unlikeTeam(User user, Team team) {
        teamLikeRepository.deleteByUserAndTeam(user, team);
    }

    @Override
    public List<Team> getLikedTeams(User user) {
        return teamLikeRepository.findAllByUser(user)
            .stream()
            .map(TeamLike::getTeam)
            .toList();
    }

    @Override
    @Transactional
    public boolean toggleTeamLike(User user, Team team) {
        boolean exists = teamLikeRepository.findByUserAndTeam(user, team).isPresent();
        if (exists) {
            teamLikeRepository.deleteByUserAndTeam(user, team);
            return false;
        } else {
            teamLikeRepository.save(TeamLike.builder()
                .user(user)
                .team(team)
                .createdAt(java.time.LocalDateTime.now())
                .build());
            return true;
        }
    }
}

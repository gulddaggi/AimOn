'use client';
import { useState } from 'react';
import TeamCard from '../molecules/team-card';
import PlayerCard from '../molecules/player-card';
import ToggleButton from '../atoms/buttons/toggle-button';
import { Team } from '@/types/team';
import { Player } from '@/types/player';

interface DashboardProps {
    teams?: Team[];
    players?: Player[];
}

export default function Dashboard({ teams = [], players = [] }: DashboardProps) {
    const [showTeams, setShowTeams] = useState(true);

    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <span className="toggle-label">팀</span>
                <ToggleButton 
                    className="team-player-toggle"
                    isToggled={!showTeams}
                    func={() => setShowTeams(!showTeams)}
                />
                <span className="toggle-label">선수</span>
            </div>
            
            <div className="dashboard-content">
                {showTeams ? (
                    <TeamCard teams={teams} />
                ) : (
                    <PlayerCard players={players} />
                )}
            </div>
        </div>
    );
}
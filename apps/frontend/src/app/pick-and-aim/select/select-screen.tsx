'use client';
import { useSearchParams } from 'next/navigation';
import PickAndAimGameSelect from '@/components/organisms/pick-and-aim-game-select';
import PickAndAimLeagueSelect from '../../../components/organisms/pick-and-aim-league-select';
import PickAndAimKeywordSelect from '../../../components/organisms/pick-and-aim-keyword-select';
import PickAndAimTeamSelect from '@/components/organisms/pick-and-aim-team-select';
import PickAndAimShotKeywordSelect from '../../../components/organisms/pick-and-aim-shot-keyword-select';

export default function SelectScreen() {
    const params = useSearchParams();
    const mode = params.get('mode') === 'aim' ? 'aim' : 'normal';
    const step = params.get('step');
    const game = params.get('game');

    if (step === 'teams' && game) {
        return <PickAndAimTeamSelect />;
    }
    if (step === 'keywords' && game) {
        return mode === 'aim' ? (
            <PickAndAimShotKeywordSelect />
        ) : (
            <PickAndAimKeywordSelect />
        );
    }
    if (step === 'league' && game) {
        return <PickAndAimLeagueSelect />;
    }

    return <PickAndAimGameSelect />;
}

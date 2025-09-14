import PickAndAimStart from '@/components/organisms/pick-and-aim-start';
import Image from 'next/image';
import logoValorant from '@/resources/pick-and-aim/logo-valorant.png';
import logoOverwatch from '@/resources/pick-and-aim/logo-overwatch2.png';

export default function PickAndAimTemplate() {
    return (
        <div className="pickAimTemplate">
            <section className="pickAimHero">
                <div className="pickAimBackdrop" />
                <div className="pickAimOverlay" />
                <div className="pickAimHeroInner">
                    <PickAndAimStart />
                    <div className="pickAimBrandRow">
                        <Image
                            src={logoValorant}
                            alt="VALORANT"
                            width={200}
                            height={120}
                            className="brandLogo"
                        />
                        <Image
                            src={logoOverwatch}
                            alt="Overwatch 2"
                            width={180}
                            height={120}
                            className="brandLogo"
                        />
                    </div>
                </div>
            </section>
        </div>
    );
}

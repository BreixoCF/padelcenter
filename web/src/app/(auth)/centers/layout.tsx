import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Centros de pádel | PadelCenter',
};

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}

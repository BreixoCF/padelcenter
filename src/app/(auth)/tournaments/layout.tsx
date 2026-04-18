import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Torneos | PadelCenter',
};

export default function Layout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}

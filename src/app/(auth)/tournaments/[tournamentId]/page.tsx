'use client';
import {
  useGetTournamentById,
  useGetTournamentPairs,
  useGetTournamentMatches,
  useRegisterPair,
} from '@/lib/api/generated/tournaments/tournaments';
import { useAuthStore } from '@/lib/auth/store';
import PageHeader from '@/components/shared/PageHeader';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function TournamentDetailPage({
  params,
}: {
  params: { tournamentId: string };
}) {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const [showRegister, setShowRegister] = useState(false);
  const [partner, setPartner] = useState('');

  const { data: tournament } = useGetTournamentById(params.tournamentId);
  const { data: pairs } = useGetTournamentPairs(params.tournamentId);
  const { data: matchesData } = useGetTournamentMatches(params.tournamentId);
  const { mutate: registerPair, isPending } = useRegisterPair();

  const pairsList = pairs ?? [];
  const matchesList = matchesData?.content ?? [];

  const handleRegister = () => {
    registerPair(
      {
        tournamentId: params.tournamentId,
        data: {
          player1Id: user?.userId ?? '',
          player2Id: partner,
        },
      },
      {
        onSuccess: () => {
          toast({ title: 'Inscripción enviada', description: 'Pendiente de confirmación' });
          setShowRegister(false);
          setPartner('');
        },
        onError: (err: any) => {
          toast({
            title: 'Error al inscribirse',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
        },
      }
    );
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      <PageHeader
        title={tournament?.name ?? ''}
        description={tournament?.format}
        action={
          tournament?.status === 'REGISTRATION_OPEN' && (
            <Button size="sm" onClick={() => setShowRegister(true)}>
              Inscribirse
            </Button>
          )
        }
      />

      <Tabs defaultValue="pairs">
        <TabsList className="mb-4">
          <TabsTrigger value="pairs">Parejas ({pairsList.length})</TabsTrigger>
          <TabsTrigger value="matches">Partidos ({matchesList.length})</TabsTrigger>
        </TabsList>

        <TabsContent value="pairs">
          <div className="space-y-2">
            {pairsList.map((pair: any) => (
              <Card key={pair.pairId}>
                <CardContent className="p-4 flex items-center justify-between">
                  <span className="text-sm">Pareja #{pair.pairId.slice(0, 8)}</span>
                  <Badge variant={pair.status === 'CONFIRMED' ? 'default' : 'outline'}>
                    {pair.status === 'CONFIRMED' ? 'Confirmada' : 'Pendiente'}
                  </Badge>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="matches">
          <div className="space-y-2">
            {matchesList.map((match: any) => (
              <Card key={match.matchId}>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">
                      Ronda {match.round}
                      {match.groupName && ` · Grupo ${match.groupName}`}
                    </span>
                    <Badge variant="outline">{match.status}</Badge>
                  </div>
                  {match.result && (
                    <p className="text-sm text-slate-500 mt-1">
                      {match.result.scoreA} — {match.result.scoreB}
                    </p>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>
      </Tabs>

      {showRegister && (
        <Dialog open onOpenChange={() => setShowRegister(false)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Inscribirse al torneo</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label>Tu ID de usuario</Label>
                <Input value={user?.userId ?? ''} disabled />
              </div>
              <div className="space-y-2">
                <Label>ID de tu pareja</Label>
                <Input
                  value={partner}
                  onChange={e => setPartner(e.target.value)}
                  placeholder="UUID del jugador pareja"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setShowRegister(false)}>
                Cancelar
              </Button>
              <Button onClick={handleRegister} disabled={!partner || isPending}>
                {isPending ? 'Inscribiendo...' : 'Confirmar'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </main>
  );
}

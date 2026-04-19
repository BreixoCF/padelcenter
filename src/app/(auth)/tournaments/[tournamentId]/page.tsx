'use client';
import { useState } from 'react';
import {
  useGetTournamentById,
  useGetTournamentPairs,
  useGetTournamentMatches,
  useRegisterPair,
  useConfirmPair,
} from '@/lib/api/generated/tournaments/tournaments';
import { useReportMatchResult } from '@/lib/api/generated/matches/matches';
import { useAuthStore } from '@/lib/auth/store';
import PageHeader from '@/components/shared/PageHeader';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { useQueryClient } from '@tanstack/react-query';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import ReportResultDialog from '@/components/tournaments/ReportResultDialog';
import BracketView from '@/components/tournaments/BracketView';

export default function TournamentDetailPage({
  params,
}: {
  params: { tournamentId: string };
}) {
  const { user, isAdmin } = useAuthStore();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [showRegister, setShowRegister] = useState(false);
  const [partner, setPartner] = useState('');
  const [reportMatch, setReportMatch] = useState<any | null>(null);

  const { data: tournament } = useGetTournamentById(params.tournamentId);
  const { data: pairs, refetch: refetchPairs } = useGetTournamentPairs(params.tournamentId);
  const { data: matchesData, refetch: refetchMatches } = useGetTournamentMatches(params.tournamentId);
  const { mutate: registerPair, isPending: isRegistering } = useRegisterPair();
  const { mutate: confirmPair } = useConfirmPair();
  const { mutate: reportMatchResult, isPending: isReporting } = useReportMatchResult();

  const pairsList = pairs ?? [];
  const matchesList = matchesData?.content ?? [];

  const handleRegister = () => {
    registerPair(
      {
        tournamentId: params.tournamentId,
        data: { player1Id: user?.userId ?? '', player2Id: partner },
      },
      {
        onSuccess: () => {
          toast({ title: 'Inscripción enviada', description: 'Pendiente de confirmación' });
          setShowRegister(false);
          setPartner('');
          refetchPairs();
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

  const handleConfirmPair = (pairId: string) => {
    confirmPair(
      { tournamentId: params.tournamentId, pairId },
      {
        onSuccess: () => {
          toast({ title: 'Pareja confirmada' });
          queryClient.invalidateQueries({
            queryKey: [`/api/v1/tournaments/${params.tournamentId}/pairs`],
          });
        },
        onError: (err: any) => {
          toast({
            title: 'Error al confirmar',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
        },
      }
    );
  };

  const handleReportResult = (data: { winnerPairId: string; scoreA: number; scoreB: number }) => {
    if (!reportMatch) return;
    reportMatchResult(
      { matchId: reportMatch.matchId, data },
      {
        onSuccess: () => {
          toast({ title: 'Resultado registrado' });
          queryClient.invalidateQueries({
            queryKey: [`/api/v1/tournaments/${params.tournamentId}/matches`],
          });
          setReportMatch(null);
        },
        onError: (err: any) => {
          toast({
            title: 'Error al reportar resultado',
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
          <TabsTrigger value="bracket">Cuadro</TabsTrigger>
        </TabsList>

        <TabsContent value="pairs">
          <div className="space-y-2">
            {pairsList.map((pair: any) => (
              <Card key={pair.pairId}>
                <CardContent className="p-4 flex items-center justify-between">
                  <span className="text-sm">Pareja #{pair.pairId.slice(0, 8)}</span>
                  <div className="flex items-center gap-2">
                    <Badge variant={pair.status === 'CONFIRMED' ? 'default' : 'outline'}>
                      {pair.status === 'CONFIRMED' ? 'Confirmada' : 'Pendiente'}
                    </Badge>
                    {isAdmin() && pair.status === 'PENDING' && (
                      <Button size="sm" variant="outline"
                        onClick={() => handleConfirmPair(pair.pairId)}>
                        Confirmar
                      </Button>
                    )}
                  </div>
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
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{match.status}</Badge>
                      {(match.status === 'SCHEDULED' || match.status === 'IN_PROGRESS') &&
                        match.pairAId && match.pairBId && (
                          <Button size="sm" variant="outline"
                            onClick={() => setReportMatch(match)}>
                            Resultado
                          </Button>
                        )}
                    </div>
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

        <TabsContent value="bracket">
          <BracketView
            matches={matchesList}
            format={tournament?.format ?? 'ELIMINATION'}
          />
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
              <Button variant="outline" onClick={() => setShowRegister(false)}>Cancelar</Button>
              <Button onClick={handleRegister} disabled={!partner || isRegistering}>
                {isRegistering ? 'Inscribiendo...' : 'Confirmar'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

      {reportMatch && (
        <ReportResultDialog
          matchId={reportMatch.matchId}
          pairAId={reportMatch.pairAId}
          pairBId={reportMatch.pairBId}
          onClose={() => setReportMatch(null)}
          onSubmit={handleReportResult}
          isPending={isReporting}
        />
      )}
    </main>
  );
}

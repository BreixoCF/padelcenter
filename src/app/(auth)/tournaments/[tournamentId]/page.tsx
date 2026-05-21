'use client';
import { useState } from 'react';
import {
  useGetTournamentById,
  useGetTournamentPairs,
  useGetTournamentMatches,
  useGetTournamentStandings,
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

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmada',
  REJECTED: 'Rechazada',
};

const MATCH_STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Programado',
  IN_PROGRESS: 'En juego',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado',
};

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
  const [teamName, setTeamName] = useState('');
  const [reportMatch, setReportMatch] = useState<any | null>(null);

  const { data: tournament } = useGetTournamentById(params.tournamentId);
  const { data: pairs, refetch: refetchPairs } = useGetTournamentPairs(params.tournamentId);
  const { data: matchesData, refetch: refetchMatches } = useGetTournamentMatches(params.tournamentId);
  const { data: standings } = useGetTournamentStandings(params.tournamentId, {
    query: { enabled: tournament?.format === 'ROUND_ROBIN' },
  });
  const { mutate: registerPair, isPending: isRegistering } = useRegisterPair();
  const { mutate: confirmPair } = useConfirmPair();
  const { mutate: reportMatchResult, isPending: isReporting } = useReportMatchResult();

  const pairsList = pairs ?? [];
  const matchesList = matchesData?.content ?? [];

  // Build a lookup map: pairId → teamName for match display
  const pairNames: Record<string, string> = {};
  for (const p of pairsList) {
    if (p.pairId && p.teamName) pairNames[p.pairId] = p.teamName;
  }

  const canRegister =
    tournament?.status === 'REGISTRATION_OPEN' || tournament?.status === 'DRAFT';

  const handleRegister = () => {
    registerPair(
      {
        tournamentId: params.tournamentId,
        data: {
          player1Id: user?.userId ?? '',
          player2Id: partner,
          teamName: teamName || undefined,
        },
      },
      {
        onSuccess: () => {
          toast({ title: 'Inscripción enviada', description: 'Pendiente de confirmación' });
          setShowRegister(false);
          setPartner('');
          setTeamName('');
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
          canRegister && (
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
          {tournament?.format === 'ROUND_ROBIN' && (
            <TabsTrigger value="standings">Clasificación</TabsTrigger>
          )}
          <TabsTrigger value="bracket">Cuadro</TabsTrigger>
        </TabsList>

        <TabsContent value="pairs">
          <div className="space-y-2">
            {pairsList.length === 0 ? (
              <p className="text-sm text-slate-500 py-4 text-center">Aún no hay parejas inscritas</p>
            ) : (
              pairsList.map((pair: any) => (
                <Card key={pair.pairId}>
                  <CardContent className="p-4 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium">
                        {pair.teamName || `Pareja #${pair.pairId.slice(0, 8)}`}
                      </p>
                      {pair.teamName && (
                        <p className="text-xs text-slate-400">{pair.pairId.slice(0, 8)}</p>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={pair.status === 'CONFIRMED' ? 'default' : 'outline'}>
                        {STATUS_LABEL[pair.status] ?? pair.status}
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
              ))
            )}
          </div>
        </TabsContent>

        <TabsContent value="matches">
          <div className="space-y-2">
            {matchesList.length === 0 ? (
              <p className="text-sm text-slate-500 py-4 text-center">Aún no hay partidos generados</p>
            ) : (
              matchesList.map((match: any) => {
                const nameA = pairNames[match.pairAId] || match.pairAId?.slice(0, 8) || 'TBD';
                const nameB = pairNames[match.pairBId] || match.pairBId?.slice(0, 8) || 'TBD';
                return (
                  <Card key={match.matchId}>
                    <CardContent className="p-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-sm font-medium">
                            {nameA} <span className="text-slate-400">vs</span> {nameB}
                          </p>
                          <p className="text-xs text-slate-400">
                            Ronda {match.round}
                            {match.groupName && ` · Grupo ${match.groupName}`}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          {match.result ? (
                            <span className="text-sm font-semibold tabular-nums">
                              {match.result.scoreA} — {match.result.scoreB}
                            </span>
                          ) : (
                            <Badge variant="outline">
                              {MATCH_STATUS_LABEL[match.status] ?? match.status}
                            </Badge>
                          )}
                          {(match.status === 'SCHEDULED' || match.status === 'IN_PROGRESS') &&
                            match.pairAId && match.pairBId && (
                              <Button size="sm" variant="outline"
                                onClick={() => setReportMatch(match)}>
                                Resultado
                              </Button>
                            )}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })
            )}
          </div>
        </TabsContent>

        {tournament?.format === 'ROUND_ROBIN' && (
          <TabsContent value="standings">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-slate-500 text-xs uppercase tracking-wide">
                    <th className="text-left py-2 px-3 font-medium">Equipo</th>
                    <th className="text-center py-2 px-2 font-medium">PJ</th>
                    <th className="text-center py-2 px-2 font-medium">G</th>
                    <th className="text-center py-2 px-2 font-medium">P</th>
                    <th className="text-center py-2 px-2 font-medium">SF</th>
                    <th className="text-center py-2 px-2 font-medium">SC</th>
                    <th className="text-center py-2 px-2 font-medium">Dif</th>
                  </tr>
                </thead>
                <tbody>
                  {(standings ?? []).map((s: any, i: number) => (
                    <tr key={s.pairId} className="border-b last:border-0 hover:bg-slate-50">
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-2">
                          <span className="text-slate-400 text-xs w-4">{i + 1}</span>
                          <span className="font-medium">{s.teamName || s.pairId.slice(0, 8)}</span>
                        </div>
                      </td>
                      <td className="text-center py-3 px-2 tabular-nums">{s.played}</td>
                      <td className="text-center py-3 px-2 tabular-nums font-medium text-green-700">{s.wins}</td>
                      <td className="text-center py-3 px-2 tabular-nums text-slate-500">{s.losses}</td>
                      <td className="text-center py-3 px-2 tabular-nums">{s.setsFor}</td>
                      <td className="text-center py-3 px-2 tabular-nums">{s.setsAgainst}</td>
                      <td className={`text-center py-3 px-2 tabular-nums font-medium ${
                        s.setDifference > 0 ? 'text-green-700' : s.setDifference < 0 ? 'text-red-600' : 'text-slate-500'
                      }`}>
                        {s.setDifference > 0 ? `+${s.setDifference}` : s.setDifference}
                      </td>
                    </tr>
                  ))}
                  {(!standings || standings.length === 0) && (
                    <tr>
                      <td colSpan={7} className="text-center text-slate-500 py-6 text-sm">
                        Sin datos de clasificación todavía
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </TabsContent>
        )}

        <TabsContent value="bracket">
          <BracketView
            matches={matchesList}
            pairs={pairsList}
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
                <Label>Nombre del equipo <span className="text-slate-400 text-xs">(opcional)</span></Label>
                <Input
                  value={teamName}
                  onChange={e => setTeamName(e.target.value)}
                  placeholder="Ej: Los Cañoneros"
                />
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
          pairAName={pairNames[reportMatch.pairAId]}
          pairBName={pairNames[reportMatch.pairBId]}
          onClose={() => setReportMatch(null)}
          onSubmit={handleReportResult}
          isPending={isReporting}
        />
      )}
    </main>
  );
}

package com.fmotech.chess.ai.oli4;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

/* OliThink 4.1.2 - Bitboard Java Version (c) Oliver Brausch 05.Jan.2004, ob112@web.de */

class OliThink {

final static int hashSize = 0x100000; // This is 16 MB
final static int hashMask = 0x0FFFFE; // This must be hashSize - 2

final static int pawnSize = 0x020000; // This is 2 MB
final static int pawnMask = 0x01FFFF; // This must be pawnSize - 1

final static int COL_N = -1;
final static int COL_W = 0;
final static int COL_B = 1;

final static int ENP_W = 0;
final static int ENP_B = 1;
final static int PAW_W = 2;
final static int PAW_B = 3;
final static int KNI_W = 4;
final static int KNI_B = 5;
final static int BIS_W = 6;
final static int BIS_B = 7;
final static int ROO_W = 8;
final static int ROO_B = 9;
final static int QUE_W = 10;
final static int QUE_B = 11;
final static int KIN_W = 12;
final static int KIN_B = 13;
final static int EMPTY = 14;

final static String piece_char = "**PpNnBbRrQqKk  ";
final static int[] piece_val = {100,-100,100,-100,300,-300,310,-310,490,-490,+900,-900,+25000,-25000,0,0};
final static int[] pos_val = {10,-10,10,-10,6,-6,8,-8,4,-4,1,-1,5,-5,0,0};

static int FROM(int x) { return ((x) & 0x3F); }
static int TO(int x) { return (((x) >> 6) & 0x3F); }
static int ONMOVE(int x) { return (((x) >> 12) & 0x01); }
static int PROMOTE(int x) { return (((x) >> 12) & 0x0F); }
static int P_TO(int x) { return (((x) >> 16) & 0x0F); }
static int IS_PROM(int x) { return (((x) >> 20) & 0x01); }
static int PIECE(int x) { return(IS_PROM(x)>0 ? 0x02 | ONMOVE(x)  : PROMOTE(x)); }
static int P_WH(int x) { return (IS_PROM(x)>0 ? 0x02 : (PROMOTE(x) & 0xE)); }
static int VAL(int x) { return (((x) >> 21) & 0x7FF); }
static int MOVEMASK(int x) { return ((x) & 0x1FFFFF); }
static boolean IDENTMV(int x, int y) { return (MOVEMASK(x) == MOVEMASK(y)); }
static int PSWAP(int x) { return (piece_val[PROMOTE(x)] - piece_val[PIECE(x)] - piece_val[P_TO(x)]); }

static int R000(int x) { return ((int)(P000 >> _r000shift[x]) & 0xFF); }
static int R090(int x) { return ((int)(P090 >> _r090shift[x]) & 0xFF); }
static int R045(int x) { return ((int)(P045 >> _r045shift[x]) & _r045lenmask[x]); }
static int R135(int x) { return ((int)(P135 >> _r135shift[x]) & _r135lenmask[x]); }
static long A000(int x) { return a000Attack[x][R000(x)]; }
static long A090(int x) { return a090Attack[x][R090(x)]; }
static long A045(int x) { return a045Attack[x][R045(x)]; }
static long A135(int x) { return a135Attack[x][R135(x)]; }

static long[][] DirA = new long[10][64];
static long[][][] Bef = new long[2][8][64];
static long[] NeiB = new long[64];
static long[] NeiR = new long[64];
static long[] LineT = new long[64];
static long[] LineD = new long[64];
static long[] LineS = new long[64];

final static int[] PAWN ={PAW_W,PAW_B};
final static int[] BISHOP ={BIS_W,BIS_B};
final static int[] KNIGHT ={KNI_W,KNI_B};
final static int[] ROOK ={ROO_W,ROO_B};
final static int[] QUEEN={QUE_W,QUE_B};
final static int[] KING ={KIN_W,KIN_B};
final static int[] ENPAS={ENP_W,ENP_B};
static int[] kingpos = new int[2];
static int[] board = new int[64];

static String boardStr=
"-----------------"+
"|r|n|b|q|k|b|n|r|"+
"-----------------"+
"|p|p|p|p|p|p|p|p|"+
"-----------------"+
"| | | | | | | | |"+
"-----------------"+
"| | | | | | | | |"+
"-----------------"+
"| | | | | | | | |"+
"-----------------"+
"| | | | | | | | |"+
"-----------------"+
"|P|P|P|P|P|P|P|P|"+
"-----------------"+
"|R|N|B|Q|K|B|N|R|"+
"-----------------";

static long[] R000BitB = new long[14];
static long[] R045BitB= new long[14];
static long[] R090BitB= new long[14];
static long[] R135BitB= new long[14];
static long QuRo, QuBi, Empty;
static long P000, P045, P090, P135;

static long[][] a000Attack= new long[64][256];
static long[][] a090Attack= new long[64][256];
static long[][] a045Attack= new long[64][256];
static long[][] a135Attack= new long[64][256];
static long[][][] aSrtAttack= new long[64][16][4];
static int[] knightmobil= new int[64];
static int[] kingmobil= new int[64];
static int[][] hitval= new int[16][16];

final static int[] r045map = { 
28,36,43,49,54,58,61,63,
21,29,37,44,50,55,59,62,
15,22,30,38,45,51,56,60,
10,16,23,31,39,46,52,57,
 6,11,17,24,32,40,47,53,
 3, 7,12,18,25,33,41,48,
 1, 4, 8,13,19,26,34,42,
 0, 2, 5, 9,14,20,27,35
};

final static int[] _r045shift = {
28,36,43,49,54,58,61,63,
21,28,36,43,49,54,58,61,
15,21,28,36,43,49,54,58,
10,15,21,28,36,43,49,54,
 6,10,15,21,28,36,43,49,
 3, 6,10,15,21,28,36,43,
 1, 3, 6,10,15,21,28,36,
 0, 1, 3, 6,10,15,21,28
};

final static int[] r045length = { 
8,7,6,5,4,3,2,1,
7,8,7,6,5,4,3,2,
6,7,8,7,6,5,4,3,
5,6,7,8,7,6,5,4,
4,5,6,7,8,7,6,5,
3,4,5,6,7,8,7,6,
2,3,4,5,6,7,8,7,
1,2,3,4,5,6,7,8 
};

final static int[] pawnpos = {
9,9,9,9,9,9,9,9,
8,8,8,8,8,8,8,8,
6,6,6,6,6,6,6,6,
4,4,5,5,5,5,4,4,
2,2,4,4,4,4,2,2,
2,2,2,2,2,2,2,2,
1,1,1,1,1,1,1,1,
0,0,0,0,0,0,0,0
};

static int[] kingmoves = {8,-9,-1,7,8,9,1,-7,-8};
static int[] knightmoves = {8,-17,-10,6,15,17,10,-6,-15};
static int[] pawnwcaps = {2,7,9};
static int[] pawnbcaps = {2,-9,-7};
static int[] pawnwmoves = {1,8};
static int[] pawnbmoves = {1,-8};
static int[] pawnwfmove = {2,8,16};
static int[] pawnbfmove = {2,-8,-16};
static int[][] runto= new int[64][64];
static int[][] direction= new int[64][64];
static int[][] pawn_val= new int[64][2];

static char[] _LSB= new char[0x10000];
static char[] _HSB= new char[0x100000];
static long[][] hashMAP= new long[64][16];
static long[][] pawnMAP= new long[64][16];
static long hashBoard;
static long hashPawn;
static long[] hashstack= new long[960];

static int[] att=new int[16];

static class HashREC  {
	public long rec = 0;
	public int move = 0;
	public short w = 0;
	public char depth = 0;
	public char flag = 0;
} 

static HashREC[] hashREC;
static HashREC[] pawnREC;

static int[] hashmv = new int[960];
static int[] movestack = new int[960];
static int[][] pv = new int[64][64];
static int[] killer = new int[64];
static int[] killer2 = new int[64];
static int[][] movelist = new int[256][64];
static int pmove;
static int pnumber;
static int[] pvlength = new int[64];
static int[] enpstack = new int[960];
static int[] cststack = new int[960];
static int[] history = new int[0x10000];
static int enpflag;
static int cstflag;
static int nstack;
static int neval;
static int nodes;
static int material;
static int mytime;
static int sd;
static int nps;
static int machine;
static int xboard;
static int comp;
static int sabort;
static int icmd;
static int doponder = 1;
static int dopost = 1;

final static int CASTLE_SHORT_W = 1;
final static int CASTLE_SHORT_B = 2;
final static int CASTLE_LONG_W = 4;
final static int CASTLE_LONG_B = 8;

static int pow2(int pow) { return (int)1<<pow; }

static long[] _r000m = new long[64];
static long[][] __r000m = new long[64][64];
static long r000m(int f) { return (long)1 << f; }

static int[] _r000shift = new int[64];
static int r000shift(int f) { return f & 0x38; }
static int t000to090(int f) { return ((~f & 0x07) << 3) + ((f >> 3) & 0x07); }

static long[] _r090m = new long[64];
static long[][] __r090m = new long[64][64];
static long r090m(int f) { return (long)1 << t000to090(f); }

static int[] _r090shift = new int[64];
static int r090shift(int f) { return r000shift(t000to090(f)); }

static long[] _r045m = new long[64];
static long[][] __r045m = new long[64][64];
static long r045m(int f) { return (long)1 << r045map[f]; }

static int[] _r045lenmask = new int[64];
static int r045lenmask(int f) { return pow2(r045length[f]) - 1; }
static int t045to135(int f) { return (f & 0x38) + ((~f) & 0x07); }

static long[] _r135m = new long[64];
static long[][] __r135m = new long[64][64];
static long r135m(int f) { return (long)1 << r045map[t045to135(f)]; }

static int[] _r135shift = new int[64];
static int r135shift(int f) { return _r045shift[t045to135(f)]; }
static int r135length(int f) { return r045length[t045to135(f)]; }

static int[] _r135lenmask=new int[64];
static int r135lenmask(int f) { return pow2(r135length(f)) - 1; }

static int r_x = 30903, r_y = 30903, r_z = 30903, r_w = 30903, r_carry = 0;
static int rand32()
{
   int t;
   r_x = r_x * 69069 + 1;
   r_y ^= r_y << 13;
   r_y ^= r_y >> 17;
   r_y ^= r_y << 5;
   t = (r_w << 1) + r_z + r_carry;
   r_carry = ((r_z >> 2) + (r_w >> 3) + (r_carry >> 2)) >> 30;
   r_z = r_w;
   r_w = t;
   return r_x + r_y + r_w;
}

static long rand64() { long c = rand32(); return rand32() | (c << 32); }

static void init_slowpieces(int i, int p, int h, int[] m) {
	int j, n;
	for (j = 1; j <= m[0]; j++) {
		n = i + m[j];
		if (n < 64 && n >= 0 && Math.abs((n & 7) - (i & 7)) <= 2) {
			if ((p == PAW_W && h == 0 && n >= 56) || (p == PAW_B && h == 0 && n < 8)) 
				aSrtAttack[i][p][2] |= r000m(n);  // non capture promotion
			else 
				aSrtAttack[i][p][h] |= r000m(n);
		}
	}
}

static char slowLSB(int i) {
	char k = (char)-1;
	while (i!=0) { k++; if ((i & 1)!=0) break; i >>= 1; }
	return k;
}

static char slowHSB(int i) {
	char k = 32;
	while (i!=0) { k--; if ((i & 0x80000000)!=0) break; i <<= 1; }
	return (char)((k < 32) ? (char)2*k+1+(((i<<1) & 0x80000000)!=0 ? 1 : 0) : 0);
}

static int bitcount(long bit) {
	int count=0;
	while (bit!=0) { bit &= (bit-1); count++; }
    return count;
}

static void init_arrays() /* precomputing stuff */
{
	int i,j,k,n,m;
	long hbit;

	hashREC = new HashREC[hashSize];
	pawnREC = new HashREC[pawnSize];

	if (hashREC==null || pawnREC==null) { printf("NO MEMORY"); System.exit(1); }
	for (i = 0; i < hashSize; i++) {
		hashREC[i] = new HashREC();
	}
	for (i = 0; i < pawnSize; i++) {
		pawnREC[i] = new HashREC();
	}
	
/*	memset(hashREC, 0, sizeof(hashREC));
	memset(aSrtAttack, 0, sizeof(aSrtAttack));
	memset(hashstack, 0, sizeof(hashstack));
	memset(hitval, 0, sizeof(hitval));
	memset(runto, -1, sizeof(runto));
	memset(direction, 0, sizeof(direction));*/
	
	for (i = 0; i < 64; i++) 
		 for (j = 0; j < 64; j++) runto[i][j] = -1;
 	for (i = 0; i < 0x100000; i++) _HSB[i] = slowHSB(i);
	for (i = 0; i < 0x10000; i++) _LSB[i] = slowLSB(i);
	for (i = 0; i < 64; i++) {
		_r000m[i] = r000m(i);
		_r045m[i] = r045m(i);
		_r090m[i] = r090m(i);
		_r135m[i] = r135m(i);
		_r000shift[i] = r000shift(i);
		_r090shift[i] = r090shift(i);
		_r135shift[i] = r135shift(i);
		_r045lenmask[i] = r045lenmask(i);
		_r135lenmask[i] = r135lenmask(i);
	}
	for (i = 0;i < 64;i++) {
		for (j = 0; j < 16; j++) {
			hashMAP[i][j] = rand64();
			pawnMAP[i][j] = ((j | 1) == PAW_B) ? hashMAP[i][j] : 0;
		}
		for (j = 0;j < 64;j++) {
			__r000m[i][j] = _r000m[i] | _r000m[j];
			__r045m[i][j] = _r045m[i] | _r045m[j];
			__r090m[i][j] = _r090m[i] | _r090m[j];
			__r135m[i][j] = _r135m[i] | _r135m[j];
		}
		NeiB[i] = NeiR[i] = _r000m[i];
		for (k = 1; k <= kingmoves[0]; k++) {
			DirA[k][i] = 0;
			for (n = -1, j = i;;) {
			int nf = j + kingmoves[k];
			if (nf < 0 || nf > 63 || (j % 8 == 0 && nf % 8 == 7) || (j % 8 == 7 && nf % 8 == 0)) 
				break;
			direction[i][nf] = kingmoves[k];
			DirA[k][i] |= _r000m[nf];
			runto[i][nf] = n;
			if (n == -1) { 
				if (Math.abs(nf-j) == 1) NeiB[i] |= _r000m[nf];
				if (nf - j == 1) NeiR[i] |= _r000m[nf];
			}
			n = j = nf;
			}
		}
		init_slowpieces(i, KNI_W, 0, knightmoves);
		init_slowpieces(i, KNI_B, 0, knightmoves);
		init_slowpieces(i, KNI_W, 1, knightmoves);
		init_slowpieces(i, KNI_B, 1, knightmoves);
		init_slowpieces(i, KIN_W, 0, kingmoves);
		init_slowpieces(i, KIN_B, 0, kingmoves);
		init_slowpieces(i, KIN_W, 1, kingmoves);
		init_slowpieces(i, KIN_B, 1, kingmoves);
		init_slowpieces(i, PAW_W, 0, pawnwmoves);
		init_slowpieces(i, PAW_B, 0, pawnbmoves);
		init_slowpieces(i, PAW_W, 1, pawnwcaps);
		init_slowpieces(i, PAW_B, 1, pawnbcaps);
		knightmobil[i] = bitcount(aSrtAttack[i][KNI_W][1]);
		kingmobil[i] = bitcount(aSrtAttack[i][KIN_W][1]) + bitcount(aSrtAttack[i][KNI_W][1]);
		pawn_val[i][COL_W] = pawnpos[63-i];
		pawn_val[i][COL_B] = -pawnpos[i];

		if (i/8 == 1) init_slowpieces(i, PAW_W, 3, pawnwfmove);
		if (i/8 == 6) init_slowpieces(i, PAW_B, 3, pawnbfmove);
		for (j = 0;j < 256;j++) {
			hbit = 0;
			for (k = i + 1; (k & 7)!=0; k++) {
				hbit |= r000m(k);
				if ((j & pow2(k & 7))!=0) break;
			}
			for (k = i - 1; (k & 7) != 7; k--) {
				hbit |= r000m(k);
				if ((j & pow2(k & 7))!=0) break;
			}
			a000Attack[i][j] = hbit;
			
			hbit = 0;
			for (k = i + 8; k < 64; k += 8) {
				hbit |= r000m(k);
				if ((j & pow2(k / 8))!=0) break;
			}
			for (k = i - 8; k >= 0; k -= 8) {
				hbit |= r000m(k);
				if ((j & pow2(k / 8))!=0) break;
			}
			a090Attack[i][j] = hbit;
			
			hbit = 0;
			for (k = i - 9, n = 0; k >= 0 && (k & 7) != 7; k -= 9, n++);
			for (k = i + 9, m = n + 1; k < 64 && (k & 7) != 0; k += 9, m++) {
				hbit |= r000m(k);
				if ((j & pow2(m))!=0) break;
			}
			for (k = i-9, m = n -1; k >= 0 && (k & 7) != 7; k -= 9, m--) {
				hbit |= r000m(k);
				if ((j & pow2(m))!=0) break;
			}
			a045Attack[i][j] = hbit;
			
			hbit = 0;
			for (k = i - 7, n = 0; k >= 0 && (k & 7) != 0; k -= 7, n++);
			for (k = i + 7, m = n + 1; k < 64 && (k & 7) != 7; k += 7, m++) {
				hbit |= r000m(k);
				if ((j & pow2(m))!=0) break;
			}
			for (k = i - 7, m = n - 1; k >= 0 && (k & 7) != 0; k -= 7, m--) {
				hbit |= r000m(k);
				if ((j & pow2(m))!=0) break;
			}
			a135Attack[i][j] = hbit;
		}
	}
	P000 = hbit = 0;
	for (i = 0; i < 64; i++) {
		LineT[i] = LineS[i] = A090(i) | _r000m[i];
		if ((i & 0x07) > 0) LineT[i] |= A090(i-1) | _r000m[i-1];
		if ((i & 0x07) < 7) LineT[i] |= A090(i+1) | _r000m[i+1];
		LineD[i] = LineT[i] & (~LineS[i]);
		hbit |= A000(i) | _r000m[i];
		Bef[0][0][i] = ~hbit | A000(i) | _r000m[i];
		Bef[1][0][i] = hbit | A000(i) | _r000m[i];
	}
	for (i = 0; i < 64; i++)
		for (j = 1; j < 8; j++) {
			Bef[0][j][i] = i + j * 8 < 64 ? Bef[0][0][i + j * 8] : 0;
			Bef[1][j][i] = i - j * 8 > 0 ? Bef[1][0][i - j * 8] : 0;
		}
	for (i = 0; i < 16; i++) {
		for (j = 0; j < 16; j++) {
			if (piece_val[j] != 0) {
				hitval[i][j] = 100 * Math.abs(piece_val[i]) / Math.abs(piece_val[j]);
			}
		}
	}
}

static boolean bioskey()
{
	boolean bReturn = false;
	if (inStart != inCounter) bReturn = true;
	return bReturn;
}

static class ReadThread implements Runnable {

	public void run() {
		for (;;) {
			OliThink.readln();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

static char lookforP(char c) {
	int c1 = piece_char.indexOf(c);
	return (char)(c1>0 ? c1 : 0);
}

static void parseBoardStr(String p) {
	int c, i = 56;
	for (int j = 0; j < p.length(); j++) {
		char pc = p.charAt(j);
		
		if (pc == '|') { 
			pc = p.charAt(++j);
			c = lookforP(pc);
			if (c!=0) board[i++] = c;
			else i -= 16;
		}
	}
}

static void upBitHelpers() {
	QuRo = R000BitB[QUE_W] | R000BitB[QUE_B] | R000BitB[ROO_W] | R000BitB[ROO_B];
	QuBi = R000BitB[QUE_W] | R000BitB[QUE_B] | R000BitB[BIS_W] | R000BitB[BIS_B];
	P000 = R000BitB[COL_W] | R000BitB[COL_B];
	P045 = R045BitB[COL_W] | R045BitB[COL_B];
	P090 = R090BitB[COL_W] | R090BitB[COL_B];
	P135 = R135BitB[COL_W] | R135BitB[COL_B];
	Empty = ~P000;
}

static void UPBITB(int x, int y) { R000BitB[x] ^= _r000m[y]; R045BitB[x] ^= _r045m[y]; R090BitB[x] ^= _r090m[y]; R135BitB[x] ^= _r135m[y]; }

static int init_board(String s) {
	int i,p,c;
	if (s!=null) parseBoardStr(s);
	hashBoard = hashPawn = hashMAP[1][0];
	Arrays.fill(R000BitB, 0);
	Arrays.fill(R045BitB, 0);
	Arrays.fill(R090BitB, 0);
	Arrays.fill(R135BitB, 0);
	
	material = comp = 0;
	for (i = 0; i < 64; i++) {
		p=board[i];
		if (p == EMPTY) continue;
		material += piece_val[p];
		if (p == KIN_W) kingpos[COL_W] = i;
		if (p == KIN_B) kingpos[COL_B] = i;
		c = p & 1;
		UPBITB(p, i);
		UPBITB(c, i);
		hashBoard ^= hashMAP[i][p];
		hashPawn ^= pawnMAP[i][p];
	}
	upBitHelpers();
	nstack = 0;
	enpflag = 0;
	enpstack[0] = 0;
	cstflag = 0;
	if (board[4] == KIN_W && board[7] == ROO_W) cstflag |= CASTLE_SHORT_W;
	if (board[4] == KIN_W && board[0] == ROO_W) cstflag |= CASTLE_LONG_W;
	if (board[60] == KIN_B && board[63] == ROO_B) cstflag |= CASTLE_SHORT_B;
	if (board[60] == KIN_B && board[56] == ROO_B) cstflag |= CASTLE_LONG_B;
	hashBoard ^= hashMAP[cstflag][14];
	cststack[0] = cstflag;
	hashstack[0] = hashBoard;
	Arrays.fill(pvlength, 0);
	Arrays.fill(killer, 0);
	Arrays.fill(killer2, 0);
	machine = COL_B;
	pmove = pnumber = 0;
	return COL_W;
}

static int get_time()
{
	return((int)new Date().getTime());
}

static void printf(String s) {
	System.out.print(s);
}

void display64(long bit) {
	char i,j;
	for (i = 56;i >= 0;i -= 8) {
		printf("\n----------------\n");
		for (j = i; j < i + 8; j++) printf(('0'+(char)((bit >> (long)j) & 1))+"|");
	}
	printf("\n----------------"+bit+"\n");
}

static void displaym(int move) {
	printf(""+(char)('a' + FROM(move) % 8)+(char)('1' + FROM(move) / 8)+(
		P_TO(move) == EMPTY ? '-' : 'x')+(char)('a' + TO(move) % 8)+(char)( '1' + TO(move) / 8));
	if (PIECE(move) != PROMOTE(move)) printf(""+piece_char.charAt(PROMOTE(move) | 1));
}

static void displayb() {
	int i,j;
	for (i = 56; i >= 0; i -= 8) {
		printf("\n-----------------\n|");
		for (j = i; j < i + 8; j++) printf(piece_char.charAt(board[j])+"|");
	}
	printf("\n-----------------\n");
	System.out.flush();
}

static void displaypv() {
	int i;
	for (i = 0; i < pvlength[0]; i++) {
		displaym(pv[0][i]); printf(" ");
	}
}

static int LSB (long bit) {
	int n = (int) bit;
	if (n!=0) {
		if ((n & 0xffff)!=0) return _LSB[n & 0xffff];
		else return 16 + _LSB[(n >> 16) & 0xffff];
	} else {
		n = (int)(bit >> 32);
		if ((n & 0xffff)!=0) return 32 + _LSB[n & 0xffff];
		else return 48 + _LSB[(n >> 16) & 0xffff];
	}
}

static int newmove(int from, int to, int p, int p_to, int promote, int ply) {

	int move = from | (to << 6) | (promote << 12) |  (p_to << 16) | ( (p!=promote ? 1:0) << 20);
	int val = (_HSB[history[move & 0xFFFF] & 0xFFFFF]);

	if (IDENTMV(move, hashmv[ply])) val += 512;
	if (IDENTMV(move, killer[ply])) val += 50;
	if (IDENTMV(move, killer2[ply])) val += 50;
	if (p_to != EMPTY) val += hitval[p_to][p] << 3;
	if (p != promote) val+= hitval[promote][QUE_W];

	return move | (val << 21);
}

static long attackbit(int i) {
	return 
	(aSrtAttack[i][PAW_W][1] & R000BitB[PAW_B]) |
	(aSrtAttack[i][PAW_B][1] & R000BitB[PAW_W]) |
	(aSrtAttack[i][KIN_W][1] & (R000BitB[KIN_W] | R000BitB[KIN_B])) |
	(aSrtAttack[i][KNI_W][1] & (R000BitB[KNI_W] | R000BitB[KNI_B])) |
	((A000(i) | A090(i)) & QuRo) |
	((A045(i) | A135(i)) & QuBi);
}

static class MyLong {
	private long myLong;
	public MyLong(long myLong) {
		this.myLong = myLong;
	}
	public void setLong(long myLong) {
		this.myLong = myLong; 
	}
	public long getLong() {
		return this.myLong;
	}
}

static int na;
static void _PTATT(MyLong aO, int p) {
	long a = aO.getLong();
	int n = LSB(a & R000BitB[p]); 
	a ^= _r000m[n];
	aO.setLong(a);
	att[na++] = n | ((p) << 12);
}

static int addattack(int i, int c, int apawn) {
	na = 0;
	long attack = attackbit(i) & R000BitB[c];
	if (apawn==0) attack &= (~R000BitB[PAWN[c]]);

	MyLong lattack = new MyLong(attack);
	while (attack!=0) {
		if ((attack & R000BitB[PAW_W | c])!=0) _PTATT(lattack, PAW_W | c);
		else if ((attack & R000BitB[KNI_W | c])!=0) _PTATT(lattack, KNI_W | c);
		else if ((attack & R000BitB[BIS_W | c])!=0) _PTATT(lattack, BIS_W | c) ;
		else if ((attack & R000BitB[ROO_W | c])!=0) _PTATT(lattack, ROO_W | c) ;
		else if ((attack & R000BitB[QUE_W | c])!=0) _PTATT(lattack, QUE_W | c) ;
		else _PTATT(lattack, KIN_W | c) ;
		attack = lattack.getLong();
	}
	return na;
}

static int attacked(int i, int on_move) {
	if ((aSrtAttack[i][PAWN[on_move]][1] & R000BitB[PAWN[on_move^1]])!=0) return 1;
	if ((aSrtAttack[i][KIN_W][1] & R000BitB[KING[on_move^1]])!=0) return 1;
	if ((aSrtAttack[i][KNI_W][1] & R000BitB[KNIGHT[on_move^1]])!=0) return 1;
	if ((((A000(i) | A090(i)) & QuRo) & R000BitB[on_move^1])!=0) return 1;
  	if ((((A045(i) | A135(i)) & QuBi) & R000BitB[on_move^1])!=0) return 1;
	return 0;
}

static long behindFigure(long a, int f, int dir) {
	switch (dir) {
		case -9: return a | (A045(f) & QuBi & DirA[1][f]);
		case -1: return a | (A000(f) & QuRo & DirA[2][f]);
		case 7: return a | (A135(f) & QuBi & DirA[3][f]);
		case 8: return a | (A090(f) & QuRo & DirA[4][f]);
		case 9: return a | (A045(f) & QuBi & DirA[5][f]);
		case 1: return a | (A000(f) & QuRo & DirA[6][f]);
		case -7: return a | (A135(f) & QuBi & DirA[7][f]);
		case -8: return a | (A090(f) & QuRo & DirA[8][f]);
		default: return a;
	}
}

static int swap(int move) {
	int to = TO(move);
	int from = FROM(move);
	int p = PIECE(move) | 1;
	long attack = attackbit(to) ^ _r000m[from];
	int[] sList=new int[64];
	int c = ONMOVE(move) ^1;
	int lastV = -piece_val[p];
	int n = 1;
	int lsb, dir;
	sList[0] = c!=0 ? PSWAP(move) : -PSWAP(move);
	dir = direction[to][from];
	if (dir!=0 && p != KIN_B) attack = behindFigure(attack, from, dir);

	while (attack!=0) {
		if ((attack & R000BitB[PAW_W | c])!=0) {lsb = LSB(attack & R000BitB[PAW_W | c]); p = PAW_B;}
		else if ((attack & R000BitB[KNI_W | c])!=0) {lsb = LSB(attack & R000BitB[KNI_W | c]); p = KNI_B;}
		else if ((attack & R000BitB[BIS_W | c])!=0) {lsb = LSB(attack & R000BitB[BIS_W | c]); p = BIS_B;}
		else if ((attack & R000BitB[ROO_W | c])!=0) {lsb = LSB(attack & R000BitB[ROO_W | c]); p = ROO_B;}
		else if ((attack & R000BitB[QUE_W | c])!=0) {lsb = LSB(attack & R000BitB[QUE_W | c]); p = QUE_B;}
		else if ((attack & R000BitB[KIN_W | c])!=0) {lsb = LSB(attack & R000BitB[KIN_W | c]); p = KIN_B;}
		else break;

		sList[n] = -sList[n-1] + lastV;
		lastV = -piece_val[p];
		attack ^= _r000m[lsb];
		dir = direction[to][lsb];
		if (dir!=0 && p != KIN_B) attack = behindFigure(attack, lsb, dir);
		n++;
		c ^= 1;
	}

	while (--n!=0) {
		if (sList[n] > -sList[n-1]) sList[n-1] = -sList[n];
	}
	return sList[0];
}

static boolean NO3ATT(int x,int  y,int  z,int c) {return attacked(x, c)==0 && attacked(y, c)==0 && attacked(z, c)==0;}

static int addcsts(int number, int on_move, int ply) {	
	if (on_move!=0) {
		if ((cstflag & CASTLE_SHORT_B)!=0) {
			if ((NeiR[61] & P000)==0 && NO3ATT(60, 61, 62, on_move))
				movelist[(number)++][ply] = newmove(60,62,KIN_B,EMPTY,KIN_B,ply);
		}
		if ((cstflag & CASTLE_LONG_B)!=0) {
			if ((NeiB[58] & P000)==0 && NO3ATT(60, 59, 58, on_move))
				movelist[(number)++][ply] = newmove(60,58,KIN_B,EMPTY,KIN_B,ply);
		}
	} else {
		if ((cstflag & CASTLE_SHORT_W)!=0) {
			if ((NeiR[5] & P000)==0 && NO3ATT(4, 5, 6, on_move))
				movelist[(number)++][ply] = newmove(4,6,KIN_W,EMPTY,KIN_W,ply);
		}
		if ((cstflag & CASTLE_LONG_W)!=0) {
			if ((NeiB[2] & P000)==0 && NO3ATT(4, 3, 2, on_move))
				movelist[(number)++][ply] = newmove(4,2,KIN_W,EMPTY,KIN_W,ply);
		}
	}
	return number;
}

static void CLRSETP(int i) {P000 ^= _r000m[i]; P045 ^= _r045m[i]; P090 ^= _r090m[i]; P135 ^= _r135m[i];}

static boolean is_bound(int i, int k, int on_move) {
	long bit; 
	int dir = direction[k][i];
	if (dir==0) return false;

	CLRSETP(i);
	bit = behindFigure(0, k, dir);
	CLRSETP(i);
	if ((bit & R000BitB[on_move^1] & behindFigure(0, i, dir))!=0) return true;
	return false;
}

static boolean IS_RANK1(int x) {return (((x) & 0x38) == 0x00);}
static boolean IS_RANK2(int x) {return (((x) & 0x38) == 0x08);}
static boolean IS_RANK4(int x) {return (((x) & 0x38) == 0x18);}
static boolean IS_RANK5(int x) {return (((x) & 0x38) == 0x20);}
static boolean IS_RANK7(int x) {return (((x) & 0x38) == 0x30);}
static boolean IS_RANK8(int x) {return (((x) & 0x38) == 0x38);}
static boolean IS_RANKL(int x, int y) {return (y!=0 ? IS_RANK1(x) : IS_RANK8(x));}
static boolean IS_RANKM(int x, int y) {return (y!=0 ? IS_RANK4(x) : IS_RANK5(x));}
static int ONEBACK(int x, int y) {return (y!=0 ? x + 0x08 : x - 0x08);}
static int ONEFORW(int x, int y) {return (y!=0 ? x - 0x08 : x + 0x08);}

static int generate_check_esc(int on_move, int ply, int number) {
	int k = kingpos[on_move];
	int c = addattack(k, on_move^1, 1);
	int from = FROM(att[0]);
	int piece = PROMOTE(att[0]) | 1;
	long movebit= aSrtAttack[k][KIN_W][1] & (~R000BitB[on_move]);

	CLRSETP(k);
	while (movebit!=0) {
		int n = LSB(movebit);
		movebit ^= _r000m[n];
		if (attacked(n, on_move)==0)
		movelist[number++][ply] = newmove(k, n, KIN_W | on_move, board[n], KIN_W | on_move, ply);
	}
	CLRSETP(k);

	if (c != 1) return number;
	c = addattack(from, on_move, 1);

	if (piece == PAW_B && IS_RANKM(from, on_move) && enpflag == ONEFORW(from, on_move)) {
		if (((NeiR[from] & R000BitB[PAW_W|on_move]))!=0 && !is_bound(from+1, k, on_move))
			movelist[number++][ply] = newmove(from+1,enpflag,PAWN[on_move],ENPAS[on_move^1],PAWN[on_move],ply);
		if (((NeiR[from-1] & R000BitB[PAW_W|on_move]))!=0 && !is_bound(from-1, k, on_move))
			movelist[number++][ply] = newmove(from-1,enpflag,PAWN[on_move],ENPAS[on_move^1],PAWN[on_move],ply);
	}

	for (;;) {
		while (c--!=0) {
			int p = PROMOTE(att[c]);
			int i = FROM(att[c]);
			if ((p | 1) == KIN_B) continue;
			if (is_bound(i, k, on_move)) continue;

			if ((p | 1) == PAW_B && IS_RANKL(from, on_move)) {
				movelist[number++][ply] = newmove(i, from, p, board[from], QUEEN[on_move], ply);
				movelist[number++][ply] = newmove(i, from, p, board[from], KNIGHT[on_move], ply);
				movelist[number++][ply] = newmove(i, from, p, board[from], ROOK[on_move], ply);
				movelist[number++][ply] = newmove(i, from, p, board[from], BISHOP[on_move], ply);
			} else
				movelist[number++][ply] = newmove(i,from,p,board[from],p,ply);
		}

		if (piece == PAW_B || piece == KNI_B || piece == KIN_B) break;
		from = runto[k][from];
		if (from == -1) break;
		c = addattack(from, on_move, 0);
	
		{   int ip = ONEBACK(from, on_move);
			if (ip > 7 && ip < 56 && board[ip] == PAWN[on_move]) {
				att[c++] = ip | PAWN[on_move] << 12;
			}
			if (IS_RANKM(from, on_move^1)) {
				ip = ONEBACK(ip, on_move);
				if (ip > 7 && ip < 56 && board[ip] == PAWN[on_move]) {
					if (board[ONEBACK(from, on_move)] == EMPTY) {
						att[c++] = ip | PAWN[on_move] << 12;
					}
				}
			}
		}
	}	
	return number;
}

static int generate_moves(int on_move, int ply, int caps) {
	int i, number=0;
	int p = PAWN[on_move];
    long attackedbit;
	long piecebit;

	long movebit, hbit = R000BitB[p];
	piecebit = R000BitB[on_move^1];
	if (caps==0) piecebit |= Empty;

	while (hbit!=0) { // First pawns
		i = LSB(hbit);
		hbit ^= _r000m[i];

		attackedbit = aSrtAttack[i][p][1] & (P000 | (enpflag!=0 ? _r000m[enpflag]:0));
		movebit = (attackedbit & (~R000BitB[on_move])) | (aSrtAttack[i][p][2] & Empty);
        if (caps==0) {
			attackedbit = aSrtAttack[i][p][0] & Empty;
			if (attackedbit!=0) attackedbit |= aSrtAttack[i][p][3] & Empty;
			movebit |= attackedbit;
		}
		
		while (movebit!=0) {
			int msb = LSB(movebit);
			movebit ^= _r000m[msb];

			if (IS_RANKL(msb, on_move)) {	// Promotions
				movelist[number++][ply] = newmove(i,msb,p,board[msb],QUEEN[on_move],ply);
				movelist[number++][ply] = newmove(i,msb,p,board[msb],KNIGHT[on_move],ply);
				movelist[number++][ply] = newmove(i,msb,p,board[msb],ROOK[on_move],ply);
				movelist[number++][ply] = newmove(i,msb,p,board[msb],BISHOP[on_move],ply);
			} else if ((((i ^ msb) & 0x07))!=0 && board[msb] == EMPTY) {  // Enpassant
				movelist[number++][ply] = newmove(i,msb,p,ENPAS[on_move^1],p,ply);
			} else { // Standard
				movelist[number++][ply] = newmove(i,msb,p,board[msb],p,ply);
			}
		}
	}	
	
	hbit=R000BitB[on_move] & (~R000BitB[p]);
	while (hbit!=0) { // The rest
		i = LSB(hbit);
		hbit ^= _r000m[i];
		p = board[i];

        if (p == QUEEN[on_move]) movebit = (A000(i) | A090(i) | A045(i) | A135(i)) & piecebit;
        else if (p == ROOK[on_move]) movebit = (A000(i) | A090(i)) & piecebit;
        else if (p == BISHOP[on_move]) movebit = (A045(i) | A135(i)) & piecebit;
        else movebit = aSrtAttack[i][p][1] & piecebit;

		while (movebit!=0) {
			int a = LSB(movebit);
			movebit ^= _r000m[a];
			movelist[number++][ply] = newmove(i, a, p, board[a], p, ply);
		}
	} 
	if (caps==0 && cstflag!=0) number = addcsts(number, on_move, ply);
	return number;
}

static void upBitboards(int move) {
	int from = FROM(move);
	int to = TO(move);
	int piece = PIECE(move);
	int p_to = P_TO(move);
	int promote = PROMOTE(move);
	int color = ONMOVE(move);

	hashBoard ^= hashMAP[to][piece];
	hashBoard ^= hashMAP[from][piece];
	hashPawn ^= pawnMAP[to][piece];
	hashPawn ^= pawnMAP[from][piece];

	R000BitB[piece] ^= __r000m[from][to];
	R000BitB[color] ^= __r000m[from][to];	
	R045BitB[piece] ^= __r045m[from][to];
	R045BitB[color] ^= __r045m[from][to];
	R090BitB[piece] ^= __r090m[from][to];
	R090BitB[color] ^= __r090m[from][to];
	R135BitB[piece] ^= __r135m[from][to];
	R135BitB[color] ^= __r135m[from][to];
	
	if ((p_to | 1) == ENP_B) {
		p_to = PAWN[color^1];
		to = ONEBACK(to, color);
	}
	if (p_to != EMPTY) {
		hashBoard ^= hashMAP[to][p_to];
		hashPawn ^= pawnMAP[to][p_to];
		UPBITB(p_to, to);
		UPBITB(color^1, to);
	}
	if (piece != promote) {
		hashBoard ^= hashMAP[to][piece];
		hashBoard ^= hashMAP[to][promote];
		hashPawn ^= pawnMAP[to][piece];
		UPBITB(piece, to);
		UPBITB(promote, to);
	}
	upBitHelpers();
}

static void domove(int move) {
	if (P_WH(move) == KIN_W) {
		rookmoves(FROM(move), TO(move), PIECE(move), 1);
		if (ONMOVE(move) == COL_W) kingpos[COL_W] = TO(move);
		else kingpos[COL_B] = TO(move);
	}
	
	board[FROM(move)] = EMPTY;
	board[TO(move)] = PROMOTE(move);
	if ((P_TO(move)|1) == ENP_B) {
		board[ONEBACK(TO(move), ONMOVE(move))] = EMPTY;
	}

	material += PSWAP(move);

	hashBoard ^= hashMAP[cstflag][14];
	hashBoard ^= hashMAP[enpflag][15];

	enpflag = 0;
	if (PIECE(move) == PAW_W && IS_RANK2(FROM(move)) && IS_RANK4(TO(move))) {	
		if ((NeiB[TO(move)] & R000BitB[PAW_B])!=0) enpflag = TO(move) - 0x08;
	}
	else if (PIECE(move) == PAW_B && IS_RANK7(FROM(move)) && IS_RANK5(TO(move))) {
		if ((NeiB[TO(move)] & R000BitB[PAW_W])!=0) enpflag = TO(move) + 0x08;
	}

	if (cstflag!=0) {
		if (FROM(move) == 4) cstflag &= (~(CASTLE_SHORT_W | CASTLE_LONG_W));
		if (FROM(move) == 60) cstflag &= (~(CASTLE_SHORT_B | CASTLE_LONG_B));
		if (FROM(move) == 0) cstflag &= (~CASTLE_LONG_W);
		if (FROM(move) == 7) cstflag &= (~CASTLE_SHORT_W);
		if (FROM(move) == 56) cstflag &= (~CASTLE_LONG_B);
		if (FROM(move) == 63) cstflag &= (~CASTLE_SHORT_B);
		if (TO(move) == 0) cstflag &= (~CASTLE_LONG_W);
		if (TO(move) == 7) cstflag &= (~CASTLE_SHORT_W);
		if (TO(move) == 56) cstflag &= (~CASTLE_LONG_B);
		if (TO(move) == 63) cstflag &= (~CASTLE_SHORT_B);
	}

	hashBoard ^= hashMAP[cstflag][14];
	hashBoard ^= hashMAP[enpflag][15];
	hashBoard ^= hashMAP[0][0];

	upBitboards(move);
	movestack[nstack++] = move;
	hashstack[nstack] = hashBoard;
	enpstack[nstack] = enpflag;
	cststack[nstack] = cstflag;
}

static void donullmove() {
	hashBoard ^= hashMAP[enpflag][15];
	enpflag = 0;
	hashBoard ^= hashMAP[0][15];
	hashBoard ^= hashMAP[0][0];
	movestack[nstack++] = 0;
	hashstack[nstack] = hashBoard;
	enpstack[nstack] = enpflag;
	cststack[nstack] = cstflag;
}

static void undonullmove() {
	hashBoard = hashstack[--nstack];
	enpflag = enpstack[nstack];
	cstflag = cststack[nstack];
}

static void undomove() {
	int move = movestack[--nstack];

	if (P_WH(move) == KIN_W) {
		rookmoves(FROM(move), TO(move), PIECE(move),0);
		if (ONMOVE(move) == COL_W) kingpos[COL_W] = FROM(move);
		else kingpos[COL_B] = FROM(move);
	}

	board[FROM(move)] = PIECE(move);

	if ((P_TO(move)|1) == ENP_B) {
		board[ONEBACK(TO(move), ONMOVE(move))] = PAWN[ONMOVE(move)^1];
		board[TO(move)] = EMPTY;
	} else {
		board[TO(move)] = P_TO(move);
	}

	material -= PSWAP(move);
	upBitboards(move);
	hashBoard = hashstack[nstack];
	enpflag = enpstack[nstack];
	cstflag = cststack[nstack];
}

static int generate_legal_moves(int on_move, int p) {
	int i, n = generate_moves(on_move, p, 0);
	for (i = 0; i < n; i++) {
		domove(movelist[i][p]);
		if (attacked(kingpos[on_move], on_move)!=0) {
			movelist[i--][p]=movelist[--n][p];
		}
		undomove();
	}
	return n;
}

static void rookmoves(int from, int to, int p, int domov) {
	int rookmove = 0;
	if (p == KIN_W && to == 2 && from == 4) {
		rookmove = newmove(0, 3, ROO_W, EMPTY, ROO_W, 0);
	} else if (p == KIN_W && to == 6 && from == 4) {
		rookmove = newmove(7, 5, ROO_W, EMPTY, ROO_W, 0);
	} else if (p == KIN_B && to == 58 && from == 60) {
		rookmove = newmove(56, 59, ROO_B, EMPTY, ROO_B, 0);
	} else if (p == KIN_B && to==62 && from==60) {
		rookmove = newmove(63, 61, ROO_B, EMPTY, ROO_B, 0);
	}
	if (rookmove == 0) return;
	hashBoard ^= hashMAP[0][0];
	if (domov!=0) { 
		domove(rookmove);
		nstack--;
		return;
	}
	movestack[nstack++] = rookmove;
	undomove();
}

static int lookUpH(int depth, HashREC hret) {
	HashREC h = 
		hashREC[(int)hashBoard & hashMask];
	
	if (h.rec != hashBoard) {
		h = 	hashREC[(int)(hashBoard & hashMask) + 1];
		if (h.rec != hashBoard) return 0;
	}
	hret.move = h.move;
	if (h.depth == depth) {
		hret.w = h.w;
		return h.flag;
	}
	return 0;
}

static void storeH(int move, int depth, int w, int flag) {
	HashREC h = 
		hashREC[(int)hashBoard & hashMask];
	if (depth < h.depth) {
		h = hashREC[(int)(hashBoard & hashMask) + 1];
	} else if (h.move!=0) {
		HashREC h2 = hashREC[(int)(hashBoard & hashMask) + 1];
		h2.depth = h.depth;
		h2.flag = h.flag;
		h2.move = h.move;
		h2.rec = h.rec;
		h2.w = h.w;
	}

	h.w = (short)w;
	h.depth = (char)depth;
	h.move = move;
	h.flag = (char)flag;
	h.rec = hashBoard;
}

static int plookUpH(HashREC hret) {
	HashREC h = pawnREC[(int)hashPawn & pawnMask];
	if (h.rec != hashPawn) return 0;
	hret.w = h.w;
	return 1;
}

static void pstoreH(int w) {
	HashREC h = pawnREC[(int)hashPawn & pawnMask];
	h.w = (short)w;
	h.rec = hashPawn;
}

static int pawneval(long pawn, long xpawn, int c) {
	long bit = pawn;
	int pval = 0;
	int i, f;

	while (bit!=0) {
		i = LSB(bit);
		bit ^= _r000m[i];
		f = (LineT[i] & Bef[c][0][i] & xpawn)!=0 ? 3 : 6; // Lola run
		pval += f * pawn_val[i][c];
		if ((LineD[i] & Bef[c^1][0][i] & pawn)==0) {  // Last Pawn
			if ((LineS[i] & xpawn & Bef[c][1][i])==0)  // ...on free file
				pval += 4 * pawn_val[i][c^1];
			if ((LineD[i] & pawn & NeiB[ONEFORW(i, c)])==0) // Lonely
				pval += 2 * pawn_val[i][c^1];
		}
		if ((LineD[i] & (~Bef[c^1][2][i]) & pawn)==0) { // Pawn 2 fields for
			pval += pawn_val[i][c^1];
			if ((LineD[i] & (~Bef[c^1][3][i]) & pawn)==0) // even 3 fields
				pval += 2*pawn_val[i][c^1];
		}
		if ((LineS[i] & Bef[c][1][i] & pawn)!=0)  // Double Pawn
			pval += pawn_val[i][c^1];
	}
	return pval;
}

static int eval(int on_move, int alpha, int beta) {
	int i, j, c, p, poseval;
	long hbit;
	int teval = on_move!=0 ? -material : material;
	if (teval - 150 >= beta) return beta;
	if (teval + 150 <= alpha) return alpha;
	neval ++;
	HashREC hret = new HashREC();
	
	if (plookUpH(hret) != 1) {
		poseval = pawneval(R000BitB[PAW_W], R000BitB[PAW_B], COL_W);
		poseval += pawneval(R000BitB[PAW_B], R000BitB[PAW_W], COL_B);
		pstoreH(poseval);
	} else poseval = hret.w;
	poseval += material;

	hbit = P000 & ~(R000BitB[PAW_W] | R000BitB[PAW_B]);
	while (hbit!=0) {		
		i = LSB(hbit);
		hbit ^= _r000m[i];
		j = board[i];
		p = j | 1;
		c = j & 1;
			if (p == KNI_B) { poseval += knightmobil[i] * pos_val[j]; continue; }
			if (p == BIS_B) { poseval += bitcount(A045(i) | A135(i)) * pos_val[j]; continue; }
			if (p == ROO_B) { poseval += bitcount(A000(i) | A090(i)) * pos_val[j]; continue; }
			if (p == KIN_B) { poseval -= kingmobil[i] * pos_val[j ^ (R000BitB[QUEEN[c^1]]==0 ? 1:0)]; }
	}

	if (R000BitB[PAW_W]==0 && poseval > 0) {
		if ((QuRo & R000BitB[COL_W])==0 && bitcount(R000BitB[COL_W]) < 3) return 0;
	} else if (R000BitB[PAW_B]==0 && poseval < 0) {
		if ((QuRo & R000BitB[COL_B])==0 && bitcount(R000BitB[COL_B]) < 3) return 0;
	}

	return on_move!=0 ? - poseval : poseval;
}

static int checkfordraw(int two) {
	int i, j;	
	for (i = nstack - 2; i > 0; i--) {
		if (P_TO(movestack[i]) != EMPTY || P_WH(movestack[i]) == PAW_W) return 0;
		if (nstack - i > 100) return 2;
		if (hashstack[i] == hashBoard) {
			if (two!=0) return 1;
			for (j = i - 1; j > 0; j--) {
				if (P_TO(movestack[j]) != EMPTY || P_WH(movestack[j]) == PAW_W) return 0;
				if (nstack - j > 100) return 2;
				if (hashstack[j] == hashBoard) return 1;
			}
		}
	}
	return 0;
}

static int pick(int ply, int n) {
	int move;
	int i, imax=0, max = 0;
	for (i = 0; i < n; i++) {
		if (VAL(movelist[i][ply]) >= max) {
			imax = i;
			max = VAL(movelist[i][ply]);
		}
	}
	move = movelist[imax][ply];
	movelist[imax][ply] = movelist[n-1][ply];
	return move;
}

static int search(int on_move, int ply, int depth, int alpha, int beta, int do_null)
{ 
	int n, i, j, w, c, hflag, nolegal;
	int bestmove = 0, move = 0;

	if ((++nodes & 0x1fff) == 0) { 
		if (bioskey()) inputmove(pmove!=0 ? ONMOVE(pmove) : COL_N);
		if (pmove==0 && (nodes*10)/nps*250 > mytime) { sabort = 1; return alpha; }
	}
	
	if (ply!=0 && depth!=0 && checkfordraw(1)!=0) return 0;
	pvlength[ply] = ply;

	c = attacked(kingpos[on_move], on_move);
	if (c!=0) depth++;
	else if (nstack!=0 && IS_PROM(movestack[nstack-1])!=0) depth++;
	if (depth < 0) depth = 0;

	HashREC hret = new HashREC();
	hflag = lookUpH(depth, hret);
	move = hret.move;
	w = hret.w;
	if (ply!=0 && hflag!=0) {
		if (hflag == 1) if (w >= beta) return beta;
		if (hflag == 2) { do_null = 0; if (w < alpha) return alpha; }
	}

	if (c==0 && do_null!=0 && depth!=0 && ply!=0 && bitcount(R000BitB[on_move] & (~R000BitB[PAWN[on_move]])) > 3) {
		donullmove();
		w = -search(on_move^1, ply+1, depth - 3 - (depth > 5 ? 1:0), -beta, -beta + 1, 0);
		undonullmove();
		if (w >= beta) {
			storeH(0, depth, w, 1);
			return w;
		}
	}

	if (ply==0) move = pv[0][0];
	hashmv[ply] = move;
	hflag = move!=0 ? 1 : 0;
	nolegal = 1;
	n = 0;

	if (depth == 0 && c==0) {
		w = eval(on_move, alpha, beta);
		if (w > alpha) alpha = w;
		if (alpha >= beta) return beta;
		n = generate_moves(on_move, ply, 1);
		if (n == 0) return w;
		hflag = 0;
	} 

	i = 0;
	do {
		if (i == 1) hflag = 0;
		if (n == 0 && hflag==0) {
			if (c!=0) 	n = generate_check_esc(on_move, ply, 0);
			else n = generate_moves(on_move, ply, 0);

			if (i == 1) {
					if (n == 1) break;
					for (j = 0; j < n; j++) {
						if (IDENTMV(movelist[j][ply], hashmv[ply])) {
							movelist[j][ply] = movelist[n-1][ply];
							break;
						}
					}
				}
			if (n == 0) break;
			}
		if (hflag==0) move = pick(ply, n-i);
		if (depth==0 && c==0 && P_TO(move) != EMPTY && swap(move) < -150) continue;

		domove(move);
		if (c==0 && attacked(kingpos[on_move], on_move)!=0) { undomove(); continue;}
		w = -search(on_move^1, ply+1, depth-1, -beta, -alpha, 1);
		if (nolegal!=0) nolegal=0;
		undomove();
		if (sabort!=0) break;

		if (w >= beta)  {
			if (P_TO(move) == EMPTY)  { 
			  killer2[ply] = killer[ply];
			  killer[ply] = move;
			}
			history[move & 0xFFFF] += depth;
			storeH(move, depth, w, 1);
			return beta;
		}
		if (w > alpha) {
			pv[ply][ply] = bestmove = move;
			for (j = ply+1; j < pvlength[ply+1]; j++) pv[ply][j] = pv[ply+1][j];
			pvlength[ply] = pvlength[ply+1];			
			alpha = w;
			if (alpha == 32499 - ply) break; 
		} 
	} while (++i < n || hflag!=0);
	if (sabort!=0) return alpha;

	if (nolegal!=0) {
		if (c!=0) return -32500 + ply;
		else if (depth!=0) return 0;
	} else
	storeH(bestmove, depth, alpha, bestmove!=0 ? 4 : 2);
	return alpha;
}

static char charAt(String s, int j) {
	char c = 0;
	try {
		c = s.charAt(j);
	} catch (StringIndexOutOfBoundsException e) {
	}
	return c;
}

static int parseMove(String s, int on_move, int n)
{
  int move;
  char c = 0;
  int i, to;
  int piece = -1;
  int from_rank = -1;
  int from_file = -1;
  int to_rank = -1;
  int to_file = -1;
  int promote = -1;

  int j = 0;
  if (charAt(s, j) >= 'A' && charAt(s, j) <= 'Z') c = lookforP(charAt(s, j));
  if (c!=0) { piece = c | on_move; j++; }
  else if (charAt(s, j) != 'O') {
     	if (charAt(s, j) > 'h' || charAt(s, j) < 'a') return 0;
  }

  if (charAt(s, j) == 'x') j++;
  else {
    if (charAt(s, j) < 'a' || charAt(s, j) > 'h' || charAt(s, j+1) < '1' || charAt(s, j+1) > '8') {
      	if (charAt(s, j) >= 'a' && charAt(s, j) <= 'h') from_file = charAt(s, j++) - 'a';
      	else if (charAt(s, j) >= '1' && charAt(s, j) <= '8') from_rank = charAt(s, j++) - '1';
    }
    if (charAt(s, j) == 'x') j++;
  }
  if (charAt(s, j) >= 'a' && charAt(s, j) <= 'h') to_file = charAt(s, j++) - 'a';
  if (charAt(s, j) >= '1' && charAt(s, j) <= '8') to_rank = charAt(s, j++) - '1';
  if (charAt(s, j) == '-' || charAt(s, j) == 'x' || charAt(s, j) == '=') j++;
  if (charAt(s, j) >= 'a' && charAt(s, j) <= 'h') { 
	from_file = to_file; to_file = charAt(s, j++) - 'a';
    	if (charAt(s, j) >= '1' && charAt(s, j) <= '8') { from_rank = to_rank; to_rank = charAt(s, j++) - '1';}
  }
  c = lookforP(charAt(s, j));
  if (c!=0 && charAt(s, j)!=0) promote = (c & 0xE) | on_move;

  to = to_file + to_rank*8;

  if (!strncmp(s,"O-O-O",5))
  {
    if (on_move==0) { to = 2; piece=KIN_W; }
    else { to = 58; piece = KIN_B; }
  }
  else if (!strncmp(s,"O-O",3))
  {
    if (on_move==0) { to = 6; piece=KIN_W; }
    else { to = 62; piece = KIN_B; }
  }

  if (piece == -1 && (from_rank == -1 || from_file == -1)) piece = PAWN[on_move];
  if (promote == -1 && piece >= 0) promote = piece;
  
  if (n==0) n = generate_legal_moves(on_move, 63);

  for (i = 0; i < n; i++)
  {
    move = movelist[i][63];
    if (to == TO(move) && (piece == -1 || piece == PIECE(move)) 
		       && (promote == -1 || promote == PROMOTE(move)) ) {
      if (from_rank >= 0 && (FROM(move) / 8) != from_rank) continue;
      if (from_file >= 0 && (FROM(move) & 7) != from_file) continue;

      return move;
    }
  }
  return 0;
}

static void parsePGN() {
	int bcolor;
	int move = 0;
	String inbuf, sLine;
	FileInputStream fpgn = null;
	try {
		fpgn = new FileInputStream("book.pgn");
		printf(" Parsing book...");
		DataInputStream r = new DataInputStream(fpgn);
		while (true) {
			sLine = r.readLine();
			StringTokenizer st = new StringTokenizer(sLine);
			while (st.hasMoreTokens()) {
				inbuf = st.nextToken();
				if (!strncmp(inbuf, "[Result", 7)) {
					inbuf = st.nextToken();
					bcolor = COL_N;
					if (!strncmp(inbuf, "\"1-0\"", 5))
						bcolor = COL_W;
					if (!strncmp(inbuf, "\"0-1\"", 5))
						bcolor = COL_B;
					if (bcolor == COL_N)
						continue;
					nstack = -1;
					while (true) {
						sLine = r.readLine();
						st = new StringTokenizer(sLine);
						while (st.hasMoreTokens()) {
							inbuf = st.nextToken();
							if (!strncmp(inbuf, "\"1-0\"", 5))
								break;
							if (!strncmp(inbuf, "\"0-1\"", 5))
								break;
							if (nstack == -1 && !strncmp(inbuf, "1.", 2))
								init_board(boardStr);
							if (nstack >= 0) {
								int iC = inbuf.indexOf('.') + 1;
								if (iC == 0 || inbuf.length() == iC) {
									inbuf = st.nextToken();			
								} else {
									inbuf = inbuf.substring(iC);
								}
								move = parseMove(inbuf, COL_W, 0);
								if (move == 0)
									break;
								if (bcolor == COL_W)
									storeH(move, 99, 0, 8);
								domove(move);
								inbuf = st.nextToken();
								move = parseMove(inbuf, COL_B, 0);
								if (move == 0)
									break;
								if (bcolor == COL_B)
									storeH(move, 99, 0, 8);
								domove(move);
								if (nstack > 20)
									break;
							}
						}
						if (nstack >=0) break;
					}
				}
			}
		}
	} catch (Exception e) {
	}
	if (fpgn != null) {
		try {
			fpgn.close();
		} catch (IOException e1) {
		}
		printf("done\n");
	}
}

static int calcmove(int on_move)
{
    int move; int n = 0, w, ms, depth = 0, alpha, beta;
	nodes = neval = sabort = 0;
	ms = get_time();
	Arrays.fill(history, 0);
	alpha = -32500;
	beta = 32500;
	pv[0][0] = 0;

	HashREC hret = new HashREC();
	
	if (pmove!=0) on_move ^= 1;
	if (pmove!=0 || lookUpH(99, hret) != 8) move = 0; 
	else {
		move = hret.move;
		n = hret.w; 
	}
	switch (generate_legal_moves(on_move, 62)) {
		case 0: return inputmove(on_move);
		case 1: if (pmove==0) move = movelist[0][62];
	}

	if (move==0) for (depth = 1; depth <= sd; depth++) {
		
		w = search(on_move, 0, depth, alpha, beta, 1);
		if (w >= beta) w = search(on_move, 0, depth, w - 100, 32500, 1);
		if (w <= alpha) w = search(on_move, 0, depth, -32500, w + 100, 1);
		if (sabort!=0 && pvlength[0] == 0) break; else n = w;

		alpha = n - 70;
		beta = n + 70;
		move = pv[0][0];
		if (dopost!=0) {
			printf(
			depth+"\t"+
			n+"\t"+
			(get_time() - ms)/10+"\t"+
			nodes+"\t");
			displaypv(); printf("\n");
		}
		System.out.flush();
		if (sabort!=0 || (pmove==0 && (nodes*10)/nps*800 > mytime)) break;
	}
	ms = get_time() - ms;
	if (ms<2) ms=2;

	while (pmove!=0 && sabort==0) inputmove(ONMOVE(pmove));

	if (icmd!=0) {
		if (pmove!=0 && icmd < 8) { if (icmd < 7) undomove(); return on_move^1; }
		return on_move;
	}

	if (pmove!=0) {
		undomove();
		domove(pmove);
		pmove = 0;
		return calcmove(on_move);
	}
	domove(move);
	
	printf((nstack+1)/2+". ... ");
	displaym(move); printf("\n");
	System.out.flush();
	
	if (nodes!=0) nps = nodes/ms * 1000 + 1;
	printf(
		(comp!=0 ? "kibitz " : "whisper ")+
 		n+"("+depth+") "+nodes+" nds "+nps+" nps "+ms+" ms "+neval+" evs\n");

	if (xboard==0) displayb();

	pnumber = doponder!=0 ? generate_legal_moves(on_move^1, 63) : 0;
	if (pnumber!=0) {
		for (n = 0; n < pnumber; n++) {
			if (IDENTMV(movelist[n][63], pv[0][1])) break;
		}
		if (n == pnumber) { pnumber = 0; return on_move^1; }
		pmove = movelist[n][63];
		domove(pmove);
		printf((comp!=0 ? "kibitz" : "whisper") + " pondering:"); displaym(pmove); printf("\n");
	}
	return on_move^1;
}

static boolean strncmp(String s, String c, int k) {
	boolean bReturn = true;
	if (k > s.length()) k = s.length();
	if (s.substring(0,k).equalsIgnoreCase(c)) bReturn = false;
	return bReturn;
}

static StringBuffer sbuf = new StringBuffer("");
static Hashtable inString = new Hashtable();
static int inStart = 0;
static int inCounter = 0;

static void readln() 
{
		byte c = 0;
		while (c != '\n') {
		try {
				c = (byte) System.in.read();
			} catch (IOException e) {
		 		e.printStackTrace();
			}
		if(c != '\n') sbuf.append((char)c);
		}

		inString.put(new Integer(inCounter++),sbuf.toString());
		sbuf.setLength(0);
}

static int inputmove(int on_move)
{
	int move; int nm, iedit = COL_N; 
	String buf = new String();
	switch (icmd) {
		case 1: return init_board(boardStr);
		case 2: undomove(); undomove(); pmove = 0; return on_move;
		case 3: machine = COL_B; pmove = 0; return COL_W;
		case 4: machine = COL_W; pmove = 0; return COL_B;
		case 5: machine = COL_N; pmove = 0; displayb(); return on_move;
		case 6: machine = on_move; pmove = 0; return on_move;
		case 7: undomove(); pnumber = generate_legal_moves(on_move, 63); return on_move^1;
		case 8: pmove = 0; return on_move;
		case 9: domove(pmove); pmove = 1; pnumber = generate_legal_moves(on_move^1, 63); return on_move;
	}
	do {
		nm = 0;
		
		while (inCounter == inStart) try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		buf = (String)inString.get(new Integer(inStart));
		inString.remove(new Integer(inStart++));
		if (!strncmp(buf,"?",1)) { if (pmove==0) sabort = 1; return on_move; }
		if (!strncmp(buf,".",1)) { if (iedit != COL_N) init_board(null); else return on_move;}
		if (!strncmp(buf,"#",1) && iedit != COL_N) for (nm = 0; nm < 64; nm++) board[nm] = EMPTY;
		if (!strncmp(buf,"c",1) && iedit != COL_N) iedit = COL_B;
		if (buf.length() < 2) return on_move;
		
		if (!strncmp(buf,"xboard",6)) { xboard = 1; printf("feature done=1\n"); return on_move; }
		if (!strncmp(buf,"quit",4)) System.exit(0);
		if (!strncmp(buf,"time",4)) { mytime = new Integer(buf.substring(5)).intValue(); nm = 1; }
		if (!strncmp(buf,"hint", 4)) { printf("Hint: "); displaym(pmove); printf("\n"); System.out.flush(); return on_move; }

		if (!strncmp(buf,"computer",8)) { comp = 1; nm = 1; }
		if (!strncmp(buf,"hard",4)) { doponder = 1; nm = 1; }
		if (!strncmp(buf,"easy",4)) { doponder = 0; nm = 1; }
		if (!strncmp(buf,"post",4)) { dopost = 1; nm = 1; }
		if (!strncmp(buf,"nopost",6)) { dopost = 0; nm = 1; }

		if (!strncmp(buf,"protover",8)) nm = 1;
		if (!strncmp(buf,"accepted",8)) nm = 1;
		if (!strncmp(buf,"random",6)) nm = 1;
		if (!strncmp(buf,"level",5)) nm = 1;
		if (!strncmp(buf,"otim",4)) nm = 1;
		if (!strncmp(buf,"result",6)) nm = 1;
		if (!strncmp(buf,"name",4)) nm = 1;
		if (!strncmp(buf,"rating",6)) nm = 1;

		if (!strncmp(buf,"edit",4)) { iedit = COL_W; continue; }
		if (!strncmp(buf,"draw",4)) return on_move;
		if (!strncmp(buf,"analyze",7)) { if (pmove==0) pnumber = generate_legal_moves(on_move, 63); pmove = 1; return on_move^1; }

		if (!strncmp(buf,"new",3)) icmd = 1;
		if (!strncmp(buf,"remove",6)) icmd = 2;
		if (!strncmp(buf,"white",2)) icmd = 3;
		if (!strncmp(buf,"black",2)) icmd = 4;
		if (!strncmp(buf,"force",5)) icmd = 5;
		if (!strncmp(buf,"go",2)) icmd = 6;
		if (!strncmp(buf,"undo",4)) icmd = 7;
		if (!strncmp(buf,"exit",4)) icmd = 8;

		if (icmd!=0) { sabort = 1; return on_move; }
		if (iedit != COL_N && buf.length() > 2) 
			board[buf.charAt(1) - 'a' + (buf.charAt(2) - '1')*8] = lookforP(buf.charAt(0)) | iedit;

	} while (nm!=0 || iedit != COL_N);
	if (on_move == COL_N) return COL_N;

	move = parseMove(buf, on_move, pmove!=0 ? pnumber : 0);
	if (move!=0) {
		if (pmove!=0) {
			if (pmove == 1) icmd = 9;
			if (IDENTMV(move, pmove)) pmove = 0;
			else { sabort = 1; pmove = move; }
			return on_move;
		}
		domove(move);
		return on_move^1;
	}
	printf("Illegal move: "+buf+"\n");
	System.out.flush();
	return on_move;
}

public static void main(String args[])
{
	int on_move;
	printf("Chess - OliThink 4.1.2 (Java)\n");
	Thread t = new Thread(new ReadThread());
	t.start();
//	signal(SIGINT, SIG_IGN);
	init_arrays();

	parsePGN();
	sd = 40;
	mytime = 18000;
	on_move = init_board(boardStr);
	nps = 100000;

    while (nstack < 950) { icmd = 0;
		if (on_move == machine || pmove!=0) on_move = calcmove(on_move);
		else on_move = inputmove(on_move);

		if (icmd!=0) on_move = inputmove(on_move);

		switch (checkfordraw(0)) {
			case 1: printf("draw\nDrawn by repitition!\n"); break;
			case 2: printf("draw\nDrawn by 50 moves rule!\n");
		}
		System.out.flush();
	}	
}
}

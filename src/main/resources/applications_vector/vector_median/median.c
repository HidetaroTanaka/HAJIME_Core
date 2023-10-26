// See LICENSE for license details.

//**************************************************************************
// Median filter (c version)
//--------------------------------------------------------------------------

void median( int n, int input[], int results[] )
{
  // Zero the ends
  results[0]   = 0;
  results[n-1] = 0;

  int vl, avl = n-2;
  int *ptr_in = input+1, *ptr_out = results+1;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e32, ta, mu"
    : "=r"(vl)
    : "r"(avl));
    // A = x[-1], x[0], x[1], ...
    asm volatile ("vle32.v v12, (%0)"
    :
    : "r"(ptr_in-1));
    // B = x[0], x[1], x[2], ...
    asm volatile ("vle32.v v13, (%0)"
    :
    : "r"(ptr_in));
    // C = x[1], x[2], x[3], ...
    asm volatile ("vle32.v v14, (%0)"
    :
    : "r"(ptr_in+1));
    // v0 = A < B
    asm volatile ("vmslt.vv v0, v12, v13");
    // v1 = B < C
    asm volatile ("vmslt.vv v1, v13, v14");
    // v2 = C < A
    asm volatile ("vmslt.vv v2, v14, v12");
    // v3 = (C<A && A<B) || (B<=A && A<=C) = (C<A && A<B) || (!C<A && !A<B)
    // v3 = !(v0 ^ v2), if v3 then A is median
    asm volatile ("vmxnor.mm v3, v0, v2");
    // v4 = (A<B && B<C) || (C<=B && B<=A) = (A<B && B<C) || (!A<B && !B<C)
    // v4 = !(v0 ^ v1), if v4 then B is median
    asm volatile ("vmxnor.mm v4, v0, v1");
    // v0 = v3
    asm volatile ("vmmv.m v0, v3");
    // v14[i] = if(v3.mask[i]) v12[i] else v14[i]
    asm volatile ("vmerge.vvm v14, v14, v12, v0");
    // v0 = v4
    asm volatile ("vmmv.m v0, v4");
    // v14[i] = if(v4.mask[i]) v13[i] else v14[i]
    asm volatile ("vmerge.vvm v14, v14, v13, v0");
    asm volatile ("vse32.v v14, (%0)"
    :
    : "r"(ptr_out));
    // increment pointer
    ptr_in += vl;
    ptr_out += vl;
    avl -= vl;
  }
  return;
}

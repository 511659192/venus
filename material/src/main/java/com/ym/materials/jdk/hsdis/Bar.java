package com.ym.materials.jdk.hsdis;

/**
 * Created by ym on 2018/6/24.
 */
public class Bar {
    int a = 1;
    static int b = 2;

    public int sum(int c) {
        return a + b + c;
    }

    public static void main(String[] args) {
        new Bar().sum(3);
    }
}

/**
 0x000000000303bdc0: mov    %eax,-0x6000(%rsp)
 0x000000000303bdc7: push   %rbp
 0x000000000303bdc8: sub    $0x30,%rsp
 0x000000000303bdcc: movabs $0x1c722d90,%rax   ;   {metadata(method data for {method} {0x000000001c722ad0} 'sum' '(I)I' in 'com/ym/materials/jdk/hsdis/Bar')}
 0x000000000303bdd6: mov    0xdc(%rax),%esi
 0x000000000303bddc: add    $0x8,%esi
 0x000000000303bddf: mov    %esi,0xdc(%rax)
 0x000000000303bde5: movabs $0x1c722ac8,%rax   ;   {metadata({method} {0x000000001c722ad0} 'sum' '(I)I' in 'com/ym/materials/jdk/hsdis/Bar')}
 0x000000000303bdef: and    $0x0,%esi
 0x000000000303bdf2: cmp    $0x0,%esi
 0x000000000303bdf5: je     0x000000000303be1c  ;*aload_0
 ; - com.ym.materials.jdk.hsdis.Bar::sum@0 (line 11)

0x000000000303bdfb: mov    0xc(%rdx),%eax     ;*getfield a
; - com.ym.materials.jdk.hsdis.Bar::sum@1 (line 11)

0x000000000303bdfe: movabs $0x76b1b1c18,%rsi  ;   {oop(a 'java/lang/Class' = 'com/ym/materials/jdk/hsdis/Bar')}
0x000000000303be08: mov    0x68(%rsi),%esi    ;*getstatic b
; - com.ym.materials.jdk.hsdis.Bar::sum@4 (line 11)

0x000000000303be0b: add    %esi,%eax
0x000000000303be0d: add    %r8d,%eax
0x000000000303be10: add    $0x30,%rsp
0x000000000303be14: pop    %rbp
0x000000000303be15: test   %eax,-0x225bd1b(%rip)        # 0x0000000000de0100
;   {poll_return}
0x000000000303be1b: retq
 */
            AREA    MYDATA, DATA, READWRITE

; 8-bit division data
num8        DCB     100         ; 8-bit number
divisor8    DCB     5           ; 8-bit divisor
result8     DCB     0           ; 8-bit result
			ALIGN   2

; 16-bit division data
num16       DCW     0x1234      ; 16-bit number (4660)
divisor16   DCW     0x12        ; 16-bit divisor (18)
result16    DCW     0           ; 16-bit result
			ALIGN   4 
				
; 32-bit division data
num32       DCD     0x12345678  ; 32-bit number
divisor32   DCD     0x1234      ; 32-bit divisor
result32    DCD     0           ; 32-bit result

            AREA    MYCODE, CODE, READONLY
            ENTRY
            EXPORT  main

main


; 8-bit division

            LDR     R0, =num8        ; Load address of num8
            LDRB    R1, [R0]         ; Load 8-bit number into R1
            LDR     R0, =divisor8
            LDRB    R2, [R0]         ; Load 8-bit divisor into R2

            UDIV    R3, R1, R2       ; Unsigned division R3 = R1 / R2

            LDR     R0, =result8
            STRB    R3, [R0]         ; Store 8-bit result


; 16-bit division

            LDR     R0, =num16
            LDRH    R1, [R0]         ; Load 16-bit number into R1
            LDR     R0, =divisor16
            LDRH    R2, [R0]         ; Load 16-bit divisor into R2

            UDIV    R3, R1, R2       ; Unsigned division R3 = R1 / R2

            LDR     R0, =result16
            STRH    R3, [R0]         ; Store 16-bit result


; 32-bit division

            LDR     R0, =num32
            LDR     R1, [R0]         ; R1 = num32
            LDR     R0, =divisor32
            LDR     R2, [R0]         ; R2 = divisor32

            UDIV    R3, R1, R2       ; R3 = R1 / R2

            LDR     R0, =result32
            STR     R3, [R0]         ; Store result

stop
            B       stop            ; Infinite loop to stop execution

            END

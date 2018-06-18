; /usr/local/bin/nasm -f macho64 64.asm && ld -macosx_version_min 10.7.0 -lSystem -o 64 64.o && ./64
global start

b:
	mov R1, 0
	mov R0, 0
	mov R2, 0
	add	R0, R2
	add	R1, R2
	add	R2, R0
	add	R1, R2
c:
	cmp	R0, R2
je e
d:
	mov A0, 0
	mul	R0, R2
	mul	R2, R1
	add	R1, A0
g:
	mul	R0, R0
	div	R0, R1
e:
	div	R1, R2
	div	R2, R0

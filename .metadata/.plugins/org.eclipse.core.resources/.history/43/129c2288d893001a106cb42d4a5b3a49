function [Origem, MEDIA, MEDIANA, MODA, DESVIO_PADRAO] = interQuartil(Origem)

Nx = size(Origem,1);

%calcula a media
mediaTemp = mean(Origem);

% 25 porcento (primeiro quartile)
Q(1) = median(Origem(find(Origem < mediaTemp)));

% 75 porcento (terceiro quartile)
Q(3) = median(Origem(find(Origem > mediaTemp)));

% interQuartil (IQR)
IQR = Q(3)-Q(1);

% outliner extremo Q1 - x < Q1 - 3*IQR
iy = find(Origem<Q(1)-3*IQR);
if length(iy)>0,
outliersQ1 = Origem(iy);
else
outliersQ1 = [,1];
end

%Origem = Origem - outliersQ1;
Origem(iy,:) = [];

% outliner extremo Q3 - x > Q1 + 3*IQR
iy = find(Origem>Q(1)+3*IQR);
if length(iy)>0,
outliersQ3 = Origem(iy);
else
outliersQ3 = [,1];
end

Noutliers = length(outliersQ1)+length(outliersQ3);


%Origem = Origem - outliersQ3;
Origem(iy,:) = [];

MEDIA = mean(Origem);

MEDIANA = median(Origem);

MODA = mode(Origem);

DESVIO_PADRAO = std(Origem);

% resultados
disp(['Media: ',num2str(MEDIA)]);
disp(['Mediana: ',num2str(MEDIANA)]);
disp(['Moda: ',num2str(MODA)]);
disp(['Desvio padrão: ',num2str(DESVIO_PADRAO)]);

disp(['Quartil 1: ',num2str(Q(1))]);
disp(['Quartil 3: ',num2str(Q(3))]);
disp(['Numero de outliers: ',num2str(Noutliers)]);

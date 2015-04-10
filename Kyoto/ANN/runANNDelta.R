rm(list = ls(all = TRUE))

#install.packages('neuralnet')
#install.packages("hydroGOF")
#install.packages("matrixStats")

library("matrixStats")
library("neuralnet")
library("hydroGOF")


#dir0 = "C:/Users/Carlos/Dropbox/AN-CV/3 DATA/6 KYOTO/[619]/Converted to TXT/2013"
dirtrain = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Train"
dirtest =  "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Test"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/ANN/Delta"

########################################################################
window_size = 3

########################################################################
setwd(dirtrain)
files = list.files(pattern='*\\[AVG-30\\].txt', recursive=TRUE)

data = NULL
for(x in files)
{
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  print(x)
}

ind = c(2,6,10)

G = data[,ind]
G = (G-colMins(data.matrix(G)))/(colMaxs(data.matrix(G))-colMins(data.matrix(G)))

G = G[complete.cases(G),] # remove missing points

G_input = NULL
G_output = NULL


for(i in 1:(nrow(G)-window_size-1))
{
  G_input = rbind(G_input,cbind(t(G[i:(i+window_size), 1]),t(G[i:(i+window_size), 2]))  )
  G_output = rbind(G_output, G[(i+window_size+1),3])
}

data = data.frame(G_input, G_output)

traininginput <-  as.data.frame(G_input)
trainingoutput <- data$G_output

#Column bind the data into one variable
trainingdata <- cbind(traininginput,trainingoutput)
trainingdata = data[complete.cases(trainingdata),]

colnames(trainingdata) <- c("G1","G2","G3","G4","G5","G6","G7","G8","Output")

#Train the neural network
#Going to have 10 hidden layers
#Threshold is a numeric value specifying the threshold for the partial
#derivatives of the error function as stopping criteria.
net.sqrt <- neuralnet(Output~G1+G2+G3+G4+G5+G6+G7+G8,trainingdata, hidden=4, threshold=0.1,
                      algorithm='rprop+',err.fct='sse', act.fct='tanh', linear.output=FALSE)
print(net.sqrt)

#Plot the neural network
#plot(net.sqrt)

########################################################################

setwd(dirtest)
files = list.files(pattern='*\\[AVG-30\\].txt', recursive=TRUE)

data = NULL
for(x in files)
{
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  print(x)
}

ind = c(2,6,10)

G = data[,ind]
G = (G-colMins(data.matrix(G)))/(colMaxs(data.matrix(G))-colMins(data.matrix(G)))

G = G[complete.cases(G),] # remove missing points

G_input = NULL
G_output = NULL


for(i in 1:(nrow(G)-window_size-1))
{
  G_input = rbind(G_input,cbind(t(G[i:(i+window_size), 1]),t(G[i:(i+window_size), 2]))  )
  G_output = rbind(G_output, G[(i+window_size+1),3])
}

data = data.frame(G_input, G_output)

testinput <-  as.data.frame(G_input)
validateoutput <- G_output

########################################################################

#Test the neural network on some training data

net.results <- compute(net.sqrt, testinput) #Run them through the neural network

#Lets see what properties net.sqrt has
ls(net.results)

#Lets see the results
print(net.results$net.result)

print(nrmse(net.results$net.result[,1], validateoutput[,1]))